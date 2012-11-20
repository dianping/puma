package com.dianping.puma.storage;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.storage.exception.StorageClosedException;

public class DefaultBinlogIndexManager implements BinlogIndexManager {
    private AtomicReference<TreeMap<BinlogInfoAndSeq, BinlogInfoAndSeq>> mainBinlogIndex         = new AtomicReference<TreeMap<BinlogInfoAndSeq, BinlogInfoAndSeq>>();
    private String                                                       mainBinlogIndexFileName = "mainBinlogIndex";
    private String                                                       mainBinlogIndexBasedir  = "/data/applogs/puma/";
    private String                                                       subBinlogIndexBaseDir   = "/data/applogs/puma/binlogindex";
    private File                                                         subBinlogFile;
    private File                                                         mainBinlogIndexFile;
    // TODO writingSubIndex
    private Properties                                                   writingSubIndex         = new Properties();                                                   ;
    private EventCodec                                                   codec;
    private volatile boolean                                             stopped                 = true;
    private BucketIndex                                                  masterIndex;
    private BucketIndex                                                  slaveIndex;

    /**
     * @param mainBinlogIndexFileName
     *            the mainBinlogIndexFileName to set
     */
    public void setMainBinlogIndexFileName(String mainBinlogIndexFileName) {
        this.mainBinlogIndexFileName = mainBinlogIndexFileName;
    }

    /**
     * @param mainBinlogIndexBasedir
     *            the mainBinlogIndexBasedir to set
     */
    public void setMainBinlogIndexBasedir(String mainBinlogIndexBasedir) {
        this.mainBinlogIndexBasedir = mainBinlogIndexBasedir;
    }

    /**
     * @param masterIndex
     *            the masterIndex to set
     */
    public void setMasterIndex(BucketIndex masterIndex) {
        this.masterIndex = masterIndex;
    }

    /**
     * @param slaveIndex
     *            the slaveIndex to set
     */
    public void setSlaveIndex(BucketIndex slaveIndex) {
        this.slaveIndex = slaveIndex;
    }

    public TreeMap<BinlogInfoAndSeq, BinlogInfoAndSeq> getBinlogIndex() {
        return mainBinlogIndex.get();
    }

    public void setBinlogIndex(TreeMap<BinlogInfoAndSeq, BinlogInfoAndSeq> binlogIndex) {
        this.mainBinlogIndex.set(binlogIndex);
    }

    @Override
    public void start() throws IOException {
        this.mainBinlogIndex.set(new TreeMap<BinlogInfoAndSeq, BinlogInfoAndSeq>());
        this.mainBinlogIndexFile = new File(mainBinlogIndexBasedir, mainBinlogIndexFileName);
        File mainbinlogIndexBasedir = new File(mainBinlogIndexBasedir);
        if (!mainbinlogIndexBasedir.exists()) {
            if (!mainbinlogIndexBasedir.mkdirs()) {
                throw new IOException(String.format("Can`t creat mainbinlogindexBasedir(%s)!",
                        mainbinlogIndexBasedir.getAbsolutePath()));
            }
        }
        this.mainBinlogIndexFile.createNewFile();
        Properties mainIndexes = new Properties();
        InputStream mbfile = null;
        try {
            mbfile = new FileInputStream(this.mainBinlogIndexFile);
            mainIndexes.load(mbfile);
        } finally {
            if (mbfile != null) {
                try {
                    mbfile.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        Set<String> mainBinlogIndexkeys = mainIndexes.stringPropertyNames();
        for (String key : mainBinlogIndexkeys) {
            String value = mainIndexes.getProperty(key);
            if (StringUtils.isNotBlank(key) && !bucketExist(value, slaveIndex, masterIndex)) {
                continue;
            }
            try {
                BinlogInfoAndSeq binlogInfoAndSeqKey = BinlogInfoAndSeq.valueOf(key);
                BinlogInfoAndSeq binlogInfoAndSeqValue = BinlogInfoAndSeq.valueOf(value);
                if (binlogInfoAndSeqKey != null && binlogInfoAndSeqValue != null) {
                    mainBinlogIndex.get().put(binlogInfoAndSeqKey, binlogInfoAndSeqValue);
                }
            } catch (Exception e) {
                // ignore
            }
        }
        TreeMap<Sequence, String> startMasterIndex = masterIndex.getIndex().get();
        for (Sequence seq : startMasterIndex.keySet()) {
            if (!inBinlogIndex(seq.longValue())) {
                addBinlogIndex(seq.longValue(), masterIndex);
            }
        }
        TreeMap<Sequence, String> startSlaveIndex = slaveIndex.getIndex().get();
        for (Sequence seq : startSlaveIndex.keySet()) {
            if (!inBinlogIndex(seq.longValue())) {
                addBinlogIndex(seq.longValue(), slaveIndex);
            }
        }
        stopped = false;
    }

    public boolean inBinlogIndex(long seq) {
        Set<BinlogInfoAndSeq> keys = this.mainBinlogIndex.get().keySet();
        for (BinlogInfoAndSeq key : keys) {
            BinlogInfoAndSeq value = (BinlogInfoAndSeq) this.mainBinlogIndex.get().get(key);
            if (value.getSeq() == seq)
                return true;
        }
        return false;
    }

    public void addBinlogIndex(long seq, BucketIndex index) throws IOException, IOException {
        Bucket bucket = index.getReadBucket(seq, true);
        ChangedEvent event = null;
        BinlogInfoAndSeq beginbinlogInfoAndSeq = null;
        BinlogInfoAndSeq endbinlogInfoAndSeq = null;
        String binlogfile = null;
        long binlogpos = -1;
        while (true) {
            try {
                byte[] data = bucket.getNext();
                event = (ChangedEvent) codec.decode(data);
                if (beginbinlogInfoAndSeq == null) {
                    beginbinlogInfoAndSeq = BinlogInfoAndSeq.getBinlogInfoAndSeq(event);
                    beginbinlogInfoAndSeq.setSeq(-1);
                    endbinlogInfoAndSeq = BinlogInfoAndSeq.getBinlogInfoAndSeq(event);
                    endbinlogInfoAndSeq.setSeq(event.getSeq());

                }
                if (StringUtils.equals(binlogfile, event.getBinlog()) || binlogpos != event.getBinlogPos()) {
                    BinlogInfoAndSeq newItem = BinlogInfoAndSeq.getBinlogInfoAndSeq(event);
                    this.writingSubIndex.put(newItem.toString(), String.valueOf(event.getSeq()));
                    binlogfile = event.getBinlog();
                    binlogpos = event.getBinlogPos();
                }
            } catch (EOFException e) {
                break;
            }
        }
        if (event == null)
            return;
        endbinlogInfoAndSeq.setBinlogInfo(event);
        TreeMap<BinlogInfoAndSeq, BinlogInfoAndSeq> newBinlogIndex = mainBinlogIndex.get();
        newBinlogIndex.put(beginbinlogInfoAndSeq, endbinlogInfoAndSeq);
        mainBinlogIndex.set(newBinlogIndex);
        openBinlogIndex(new Sequence(endbinlogInfoAndSeq.getSeq()));
        // TODO flushIndex
        flushBinlogIndex(beginbinlogInfoAndSeq);
        // TODO merge into flushIndex
    }

    private boolean bucketExist(String s, BucketIndex slaveIndex, BucketIndex masterIndex) {
        BinlogInfoAndSeq binlogInfoAndSeq = BinlogInfoAndSeq.valueOf(s);
        Sequence sequence = new Sequence(Long.valueOf(binlogInfoAndSeq.getSeq()).longValue());
        if (slaveIndex.getIndex().get().get(sequence) == null && masterIndex.getIndex().get().get(sequence) == null) {
            return false;
        } else {
            return true;
        }
    }

    private void updateStartBinlogInfoIndex(Bucket bucket) {
        TreeMap<BinlogInfoAndSeq, BinlogInfoAndSeq> newBinlogInfoIndexes = new TreeMap<BinlogInfoAndSeq, BinlogInfoAndSeq>(
                mainBinlogIndex.get());
        newBinlogInfoIndexes.put(bucket.getStartingBinlogInfoAndSeq(), bucket.getCurrentWritingBinlogInfoAndSeq());
        mainBinlogIndex.set(newBinlogInfoIndexes);
    }

    public void updateMainBinlogIndex(Bucket bucket) {
        if (mainBinlogIndex.get().get(bucket.getStartingBinlogInfoAndSeq()) == null) {
            updateStartBinlogInfoIndex(bucket);
        } else {
            BinlogInfoAndSeq temp = mainBinlogIndex.get().get(bucket.getStartingBinlogInfoAndSeq());
            temp.setServerId(bucket.getCurrentWritingBinlogInfoAndSeq().getServerId());
            temp.setBinlogFile(bucket.getCurrentWritingBinlogInfoAndSeq().getBinlogFile());
            temp.setBinlogPosition(bucket.getCurrentWritingBinlogInfoAndSeq().getBinlogPosition());
        }
    }

    // TODO openSubIndex
    public void openBinlogIndex(Sequence seq) throws IOException {
        this.subBinlogFile = new File(this.subBinlogIndexBaseDir, seq.convertToSubBinlogIndexPath());
        if (!this.subBinlogFile.getParentFile().exists()) {
            if (!this.subBinlogFile.getParentFile().mkdirs()) {
                throw new IOException(String.format("Can't create writeBucket's parent(%s)!",
                        this.subBinlogFile.getParent()));
            }
        }
    }

    public long readBinlogIndex(Sequence seq, BinlogInfoAndSeq binlogInfoAndSeq) throws IOException {
        Properties result = new Properties();
        File bfile = new File(subBinlogIndexBaseDir, seq.convertToSubBinlogIndexPath());
        if (bfile.exists()) {
            InputStream inStream = new FileInputStream(bfile);
            result.load(inStream);
            inStream.close();
        } else {
            TreeMap<BinlogInfoAndSeq, BinlogInfoAndSeq> now = this.mainBinlogIndex.get();
            if (binlogContain(binlogInfoAndSeq, now.lastEntry().getKey(), now.lastEntry().getValue())) {
                result = this.writingSubIndex;
            }
        }
        if (result == null)
            return -1;
        String temp = result.getProperty(binlogInfoAndSeq.toString());
        if (temp == null) {
            return -1;
        } else {
            return Long.valueOf(temp).longValue();
        }
    }

    private Boolean binlogContain(BinlogInfoAndSeq value, BinlogInfoAndSeq start, BinlogInfoAndSeq end) {
        if (value.getServerId() != start.getServerId())
            return false;
        if (value.getBinlogFile().compareTo(start.getBinlogFile()) < 0
                || value.getBinlogFile().compareTo(end.getBinlogFile()) > 0)
            return false;
        if (value.getBinlogFile().compareTo(end.getBinlogFile()) == 0
                && value.getBinlogPosition() > end.getBinlogPosition())
            return false;
        return true;
    }

    public long tranBinlogIndexToSeq(BinlogInfoAndSeq binlogInfoAndSeq) throws StorageClosedException, IOException {
        TreeMap<BinlogInfoAndSeq, BinlogInfoAndSeq> map = this.mainBinlogIndex.get();
        Set<BinlogInfoAndSeq> keys = map.keySet();
        for (BinlogInfoAndSeq key : keys) {
            BinlogInfoAndSeq value = (BinlogInfoAndSeq) map.get(key);
            if (binlogContain(binlogInfoAndSeq, key, value)) {
                Sequence seq = new Sequence(value.getSeq());
                return readBinlogIndex(seq, binlogInfoAndSeq);
            }
        }
        return -1;
    }

    public void flushBinlogIndex(BinlogInfoAndSeq binlogInfoAndSeq) throws IOException {
        BinlogInfoAndSeq value = mainBinlogIndex.get().get(binlogInfoAndSeq);
        try {
            OutputStream sbindex = new FileOutputStream(this.subBinlogFile);
            this.writingSubIndex.store(sbindex, "write to subbinlogfile");
            sbindex.close();
            OutputStream mbindex = new FileOutputStream(this.mainBinlogIndexFile, true);
            Properties mbindexitem = new Properties();
            mbindexitem.put(binlogInfoAndSeq.toString(), value.toString());
            mbindexitem.store(mbindex, "write a mainbinglogitem");
            mbindex.close();
            this.writingSubIndex.clear();
        } catch (IOException e) {
            // TODO log.err(msg, throwable)
            System.err.println("write to binlogfile fail!");
        }
    }

    public void deleteSubBinlogIndexFile(Sequence seq) {
        // TODO convertToSubIndexPath
        File bindex = new File(subBinlogIndexBaseDir, seq.convertToSubBinlogIndexPath());
        bindex.delete();
    }

    // TODO is there any problem?
    public void deleteBinlogIndex(String path) {
        // TODO refactor all convertToSequence & convertToPath into Sequence
        Sequence temp = Sequence.convertToSequence(path);
        long deleteitem = temp.longValue();
        deleteSubBinlogIndexFile(temp);

        // TODO generic
        // TODO copy on write, use tmp hashmap
        TreeMap<BinlogInfoAndSeq, BinlogInfoAndSeq> map = this.mainBinlogIndex.get();
        Set<BinlogInfoAndSeq> keys = map.keySet();
        for (BinlogInfoAndSeq key : keys) {
            BinlogInfoAndSeq value = map.get(key);
            if (value.getSeq() == deleteitem) {
                map.remove(key);
                break;
            }
        }
        mainBinlogIndex.set(map);
        try {
            if (this.mainBinlogIndexFile.length() > 1024 * 1024 * 200) {
                File newmbindex = new File(this.mainBinlogIndexBasedir, this.mainBinlogIndexFileName + "_bak");
                OutputStream mbindex = new FileOutputStream(newmbindex);
                this.writingSubIndex.store(mbindex, "store prop to backend");
                // TODO delete original file
                newmbindex.renameTo(this.mainBinlogIndexFile);
            }
        } catch (IOException e) {
            // Dont`t do anything
        }
    }

    public void stop() {
        if (stopped) {
            return;
        }

        stopped = true;
    }

    public void updateSubBinlogIndex(BinlogInfoAndSeq bpas) throws IOException {
        this.writingSubIndex.put(bpas.toString(), String.valueOf(bpas.getSeq()));
    }

    public String getSubBinlogIndexBaseDir() {
        return subBinlogIndexBaseDir;
    }

    public void setSubBinlogIndexBaseDir(String subBinlogIndexBaseDir) {
        this.subBinlogIndexBaseDir = subBinlogIndexBaseDir;
    }

    public EventCodec getCodec() {
        return codec;
    }

    public void setCodec(EventCodec codec) {
        this.codec = codec;
    }

    public File getMainBinlogIndexFile() {
        return mainBinlogIndexFile;
    }

    public void setMainBinlogIndexFile(File mainBinlogIndexFile) {
        this.mainBinlogIndexFile = mainBinlogIndexFile;
    }
}
