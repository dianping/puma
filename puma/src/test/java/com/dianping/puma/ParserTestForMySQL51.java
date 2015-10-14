package com.dianping.puma;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.puma.parser.type.BigIntTypeDebug;
import com.dianping.puma.parser.type.BinaryTypeDebug;
import com.dianping.puma.parser.type.BitTypeDebug;
import com.dianping.puma.parser.type.BlobTypeDebug;
import com.dianping.puma.parser.type.CharTypeDebug;
import com.dianping.puma.parser.type.DateTimeTypeDebug;
import com.dianping.puma.parser.type.DateTypeDebug;
import com.dianping.puma.parser.type.DecimalTypeDebug;
import com.dianping.puma.parser.type.DoubleTypeDebug;
import com.dianping.puma.parser.type.EnumTypeDebug;
import com.dianping.puma.parser.type.FloatTypeDebug;
import com.dianping.puma.parser.type.IntTypeDebug;
import com.dianping.puma.parser.type.LongBlobTypeDebug;
import com.dianping.puma.parser.type.LongTextTypeDebug;
import com.dianping.puma.parser.type.MediumBlobTypeDebug;
import com.dianping.puma.parser.type.MediumIntTypeDebug;
import com.dianping.puma.parser.type.MediumTextTypeDebug;
import com.dianping.puma.parser.type.SetTypeDebug;
import com.dianping.puma.parser.type.SmallIntTypeDebug;
import com.dianping.puma.parser.type.TextTypeDebug;
import com.dianping.puma.parser.type.TimeTypeDebug;
import com.dianping.puma.parser.type.TimestampTypeDebug;
import com.dianping.puma.parser.type.TinyBlobTypeDebug;
import com.dianping.puma.parser.type.TinyIntTypeDebug;
import com.dianping.puma.parser.type.TinyTextTypeDebug;
import com.dianping.puma.parser.type.VarBinaryTypeDebug;
import com.dianping.puma.parser.type.VarcharTypeDebug;
import com.dianping.puma.parser.type.YearTypeDebug;


/***
 * mysql5.1 parser function test
 * @author qi.yin
 *
 */

@RunWith(Suite.class)
@SuiteClasses({
	//int
	BigIntTypeDebug.class,
	IntTypeDebug.class,
	MediumIntTypeDebug.class,
	SmallIntTypeDebug.class,
	TinyIntTypeDebug.class,
	//float
	DecimalTypeDebug.class,
	DoubleTypeDebug.class,
	FloatTypeDebug.class,
	//char
	CharTypeDebug.class,
	VarcharTypeDebug.class,
	//text
	TextTypeDebug.class,
	LongTextTypeDebug.class,
	MediumTextTypeDebug.class,
	TinyTextTypeDebug.class,
	//blob
	BlobTypeDebug.class,
	LongBlobTypeDebug.class,
	MediumBlobTypeDebug.class,
	TinyBlobTypeDebug.class,
	//binary
	BinaryTypeDebug.class,
	VarBinaryTypeDebug.class,
	//time
	DateTimeTypeDebug.class,
	TimeTypeDebug.class,
	TimestampTypeDebug.class,
	DateTypeDebug.class,
	YearTypeDebug.class,
	//bit,set,enum
	BitTypeDebug.class,
	SetTypeDebug.class,
	EnumTypeDebug.class
})
public class ParserTestForMySQL51 {

}
