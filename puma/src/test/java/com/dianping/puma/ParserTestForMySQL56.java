package com.dianping.puma;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.puma.parser.type.DateTime2TypeTest;
import com.dianping.puma.parser.type.Time2TypeTest;
import com.dianping.puma.parser.type.Timestamp2TypeTest;


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
	Timestamp2TypeTest.class

})
public class ParserTestForMySQL56 {

}
