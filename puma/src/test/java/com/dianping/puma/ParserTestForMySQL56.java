package com.dianping.puma;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.puma.parser.type.DateTime2TypeDebug;
import com.dianping.puma.parser.type.Time2TypeDebug;
import com.dianping.puma.parser.type.Timestamp2TypeDebug;


/***
 * mysql5.6 parser function test
 * @author qi.yin
 *
 */

@RunWith(Suite.class)
@SuiteClasses({
	
	//5.6
	DateTime2TypeDebug.class,
	Time2TypeDebug.class,
	Timestamp2TypeDebug.class

})
public class ParserTestForMySQL56 {

}
