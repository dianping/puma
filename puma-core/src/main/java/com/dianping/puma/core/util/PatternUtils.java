package com.dianping.puma.core.util;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatternUtils {

	private static final Logger LOG = LoggerFactory.getLogger(PatternUtils.class);
	private static Map<String, WeakReference<Pattern>> patterns = new ConcurrentHashMap<String, WeakReference<Pattern>>();

	private static Perl5Matcher matcher = new Perl5Matcher();
	
	public static Pattern getPattern(String strPattern) {
		if (patterns.containsKey(strPattern) && patterns.get(strPattern).get() != null) {
			return patterns.get(strPattern).get();
		} else {
			PatternCompiler pc = new Perl5Compiler();
			try {
				Pattern pattern = pc.compile(strPattern, Perl5Compiler.CASE_INSENSITIVE_MASK
						| Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.SINGLELINE_MASK);
				patterns.put(strPattern, new WeakReference<Pattern>(pattern));
				return pattern;
			} catch (MalformedPatternException e) {
				LOG.error("puma pattern error.");
				return null;
			}
		}

	}
	
	public static boolean isMatches(String strQuery,String strPattern){
		return matcher.matches(strQuery, PatternUtils.getPattern(strPattern));
	}
}