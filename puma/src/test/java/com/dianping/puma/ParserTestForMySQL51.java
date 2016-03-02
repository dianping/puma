package com.dianping.puma;

import com.dianping.puma.parser.type.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


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
