package com.dianping.puma.server.Position;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class PositionFileUtils {

	private Map<Long, PositionInfor> PositionFile = new HashMap<Long, PositionInfor>();

	public PositionFileUtils(Map<Long, PositionInfor> positionFile) {
		this.PositionFile = positionFile;
	}

	public PositionFileUtils() {

	}

	public Map<Long, PositionInfor> getPositionFile() {
		return this.PositionFile;
	}

	public void setPositionFile(Map<Long, PositionInfor> positionFile) {
		this.PositionFile = positionFile;
	}

	public PositionInfor read(Long serverID) {
		return this.PositionFile.get(serverID);
	}

	public void write(Long serverID, PositionInfor positionInfor) {
		this.PositionFile.put(serverID, positionInfor);

	}

	public PositionInfor readFile(String filepath, Long serverID) {
		PositionInfor positioninfor = new PositionInfor();
		File f = new File(filepath + serverID);

		if (!f.exists()) {
			try {
				f.createNewFile();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			positioninfor.binlogFileName = br.readLine();
			positioninfor.binlogPosition = Long.parseLong(br.readLine());

			br.close();
			fr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return positioninfor;
	}

	public void writeFile(String filepath, Long serverID,
			PositionInfor positionInfor) {

		File f = new File(filepath + serverID);

		if (!f.exists()) {
			try {
				f.createNewFile();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			FileWriter fw = new FileWriter(f);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(positionInfor.binlogFileName);
			bw.newLine();
			bw.write(String.valueOf(positionInfor.binlogPosition));
			bw.newLine();

			bw.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public int getsizeofPositionFile() {
		return this.PositionFile.size();
	}
}