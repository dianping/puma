package com.dianping.puma.integration;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.puma.integration.function.BigIntTypeTest;
import com.dianping.puma.integration.function.BinaryTypeTest;
import com.dianping.puma.integration.function.BitTypeTest;
import com.dianping.puma.integration.function.BlobTypeTest;
import com.dianping.puma.integration.function.CharTypeTest;
import com.dianping.puma.integration.function.DateTime2TypeTest;
import com.dianping.puma.integration.function.DateTimeTypeTest;
import com.dianping.puma.integration.function.DateTypeTest;
import com.dianping.puma.integration.function.DecimalTypeTest;
import com.dianping.puma.integration.function.DoubleTypeTest;
import com.dianping.puma.integration.function.EnumTypeTest;
import com.dianping.puma.integration.function.FloatTypeTest;
import com.dianping.puma.integration.function.IntTypeTest;
import com.dianping.puma.integration.function.LongBlobTypeTest;
import com.dianping.puma.integration.function.LongTextTypeTest;
import com.dianping.puma.integration.function.MediumBlobTypeTest;
import com.dianping.puma.integration.function.MediumIntTypeTest;
import com.dianping.puma.integration.function.MediumTextTypeTest;
import com.dianping.puma.integration.function.SetTypeTest;
import com.dianping.puma.integration.function.SmallIntTypeTest;
import com.dianping.puma.integration.function.TextTypeTest;
import com.dianping.puma.integration.function.Time2TypeTest;
import com.dianping.puma.integration.function.TimeTypeTest;
import com.dianping.puma.integration.function.Timestamp2TypeTest;
import com.dianping.puma.integration.function.TimestampTypeTest;
import com.dianping.puma.integration.function.TinyBlobTypeTest;
import com.dianping.puma.integration.function.TinyIntTypeTest;
import com.dianping.puma.integration.function.TinyTextTypeTest;
import com.dianping.puma.integration.function.VarBinaryTypeTest;
import com.dianping.puma.integration.function.VarcharTypeTest;
import com.dianping.puma.integration.function.YearTypeTest;


/***
 * mysql5.6 parser function test
 * @author qi.yin
 *
 */

@RunWith(Suite.class)
@SuiteClasses({
	
	//5.6
	DateTime2TypeTest.class,
	Time2TypeTest.class,
	Timestamp2TypeTest.class,

})
public class ParserFunctionTest5_6 {

}
