import junit.framework.Assert;

import org.junit.Test;

import com.dianping.puma.server.Position.PositionFileUtils;
import com.dianping.puma.server.Position.PositionInfor;

public class PositionFileUtilsTest {
	@Test
	public void testRead() {

		PositionInfor a = new PositionInfor(1, "file.txt");

		PositionFileUtils positionfileutil = new PositionFileUtils();

		long num = 123;
		positionfileutil.write(num, a);

		Assert.assertEquals(
				"PositionInfor [binlogFileName=file.txt, binlogPosition=1]",
				positionfileutil.read(num).toString());
	}

	@Test
	public void testWrite() {
		PositionInfor a = new PositionInfor(1, "file.txt");

		PositionFileUtils positionfileutil = new PositionFileUtils();

		long num = 123;
		positionfileutil.write(num, a);

		// value overwrite

		a.setBinlogFileName("file1.txt");
		a.setBinlogPosition(2);

		positionfileutil.write(num, a);
		Assert.assertEquals(
				"PositionInfor [binlogFileName=file1.txt, binlogPosition=2]",
				positionfileutil.read(num).toString());
	}

	@Test
	public void testSizeReturn0() {
		PositionFileUtils positionfileutil = new PositionFileUtils();
		Assert.assertEquals(0, positionfileutil.getsizeofPositionFile());
	}

	@Test
	public void testSizeReturn1() {
		PositionInfor a = new PositionInfor(1, "file.txt");

		PositionFileUtils positionfileutil = new PositionFileUtils();

		long num = 123;
		positionfileutil.write(num, a);

		// value overwrite

		a.setBinlogFileName("file1.txt");
		a.setBinlogPosition(2);

		positionfileutil.write(num, a);
		Assert.assertEquals(1, positionfileutil.getsizeofPositionFile());

	}

	@Test
	public void testReadFile() {
		PositionFileUtils positionfileutil = new PositionFileUtils();
		long num = 1;
		Assert.assertEquals("3456+erti", positionfileutil.readFile("E:/", num)
				.getBinlogFileName());
		Assert.assertEquals(1, positionfileutil.readFile("E:/", num)
				.getBinlogPosition());

	}

	@Test
	public void testWriteFile() {
		PositionFileUtils positionfileutil = new PositionFileUtils();
		long num = 1;
		PositionInfor pos = new PositionInfor(num, "3459038405+erti");
		positionfileutil.writeFile("E:/", num, pos);

		Assert.assertEquals("3459038405+erti", positionfileutil.readFile("E:/", num)
				.getBinlogFileName());
		Assert.assertEquals(1, positionfileutil.readFile("E:/", num)
				.getBinlogPosition());
	}

}