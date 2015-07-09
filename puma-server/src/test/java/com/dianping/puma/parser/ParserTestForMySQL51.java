package com.dianping.puma.parser;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.puma.parser.type.BigIntTypeTest;
import com.dianping.puma.parser.type.BinaryTypeTest;
import com.dianping.puma.parser.type.BitTypeTest;
import com.dianping.puma.parser.type.BlobTypeTest;
import com.dianping.puma.parser.type.CharTypeTest;
import com.dianping.puma.parser.type.DateTimeTypeTest;
import com.dianping.puma.parser.type.DateTypeTest;
import com.dianping.puma.parser.type.DecimalTypeTest;
import com.dianping.puma.parser.type.DoubleTypeTest;
import com.dianping.puma.parser.type.EnumTypeTest;
import com.dianping.puma.parser.type.FloatTypeTest;
import com.dianping.puma.parser.type.IntTypeTest;
import com.dianping.puma.parser.type.LongBlobTypeTest;
import com.dianping.puma.parser.type.LongTextTypeTest;
import com.dianping.puma.parser.type.MediumBlobTypeTest;
import com.dianping.puma.parser.type.MediumIntTypeTest;
import com.dianping.puma.parser.type.MediumTextTypeTest;
import com.dianping.puma.parser.type.SetTypeTest;
import com.dianping.puma.parser.type.SmallIntTypeTest;
import com.dianping.puma.parser.type.TextTypeTest;
import com.dianping.puma.parser.type.TimeTypeTest;
import com.dianping.puma.parser.type.TimestampTypeTest;
import com.dianping.puma.parser.type.TinyBlobTypeTest;
import com.dianping.puma.parser.type.TinyIntTypeTest;
import com.dianping.puma.parser.type.TinyTextTypeTest;
import com.dianping.puma.parser.type.VarBinaryTypeTest;
import com.dianping.puma.parser.type.VarcharTypeTest;
import com.dianping.puma.parser.type.YearTypeTest;


/***
 * mysql5.1 parser function test
 * @author qi.yin
 *
 */

@RunWith(Suite.class)
@SuiteClasses({
	//int
	BigIntTypeTest.class,
	IntTypeTest.class,
	MediumIntTypeTest.class,
	SmallIntTypeTest.class,
	TinyIntTypeTest.class,
	//float
	DecimalTypeTest.class,
	DoubleTypeTest.class,
	FloatTypeTest.class,
	//char
	CharTypeTest.class,
	VarcharTypeTest.class,
	//text
	TextTypeTest.class,
	LongTextTypeTest.class,
	MediumTextTypeTest.class,
	TinyTextTypeTest.class,
	//blob
	BlobTypeTest.class,
	LongBlobTypeTest.class,
	MediumBlobTypeTest.class,
	TinyBlobTypeTest.class,
	//binary
	BinaryTypeTest.class,
	VarBinaryTypeTest.class,
	//time
	DateTimeTypeTest.class,
	TimeTypeTest.class,
	TimestampTypeTest.class,
	DateTypeTest.class,
	YearTypeTest.class,
	//bit,set,enum
	BitTypeTest.class,
	SetTypeTest.class,
	EnumTypeTest.class
})
public class ParserTestForMySQL51 {

}
