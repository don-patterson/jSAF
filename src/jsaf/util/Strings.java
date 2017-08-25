// Copyright (C) 2011-2017 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Apparently there are still a few things that haven't yet been packed into java.lang.String!
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.2
 */
public class Strings {
    /**
     * Line-feed character (as a String).
     *
     * @since 1.3
     */
    public static final String LF = System.getProperty("line.separator");

    /**
     * The line separator on the local machine.
     *
     * @since 1.2
     * @deprecated since 1.3.5. Use LF instead.
     */
    @Deprecated public static final String LOCAL_CR = LF;

    /**
     * Escape character (as a String).
     *
     * @since 1.2
     */
    public static final String ESCAPE = "\\";

    /**
     * Open curly-bracket. Declaring this as a constant makes it easier to use in code.
     *
     * @since 1.2
     */
    public static final String OPEN = "{";

    /**
     * Close curly-bracket. Declaring this as a constant makes it easier to use in code.
     *
     * @since 1.2
     */
    public static final String CLOSE = "}";

    private static final char[] REGEX_CHARS = {'\\', '^', '.', '$', '|', '(', ')', '[', ']', '{', '}', '*', '+', '?'};
    private static final String[] REGEX_STRS = {ESCAPE, "^", ".", "$", "|", "(", ")", "[", "]", OPEN, CLOSE, "*", "+", "?"};
    private static final String QUALIFIER_PATTERN = "[0-9]+,{0,1}[0-9]*";

    /**
     * An ascending Comparator for Strings.
     *
     * @since 1.2
     */
    public static final Comparator<String> COMPARATOR = new StringComparator(true);

    /**
     * ASCII charset.
     *
     * @since 1.2
     */
    public static final Charset ASCII = Charset.forName("US-ASCII");

    /**
     * UTF8 charset.
     *
     * @since 1.2
     */
    public static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * UTF16 charset.
     *
     * @since 1.2
     */
    public static final Charset UTF16 = Charset.forName("UTF-16");

    /**
     * UTF16 Little Endian charset.
     *
     * @since 1.2
     */
    public static final Charset UTF16LE = Charset.forName("UTF-16LE");

    /**
     * Sort the array from A-&gt;Z (ascending ordering).
     *
     * @since 1.2
     */
    public static final String[] sort(String[] array) {
	return sort(array, true);
    }

    /**
     * Arrays can be sorted ascending or descending.
     *
     * @param asc true for ascending (A-&gt;Z), false for descending (Z-&gt;A).
     *
     * @since 1.2
     */
    public static final String[] sort(String[] array, boolean asc) {
	Arrays.sort(array, new StringComparator(asc));
	return array;
    }

    /**
     * A StringTokenizer operates on single-character tokens. This acts on a delimiter that is a multi-character String.
     *
     * @since 1.2
     */
    public static Iterator<String> tokenize(String target, String delimiter) {
	return new StringTokenIterator(target, delimiter);
    }

    /**
     * Gives you an option to keep any zero-length tokens at the ends of the target, if it begins or ends with the delimiter.
     * This guarantees that you get one token for every instance of the delimiter in the target String.
     *
     * @since 1.2
     */
    public static Iterator<String> tokenize(String target, String delimiter, boolean trim) {
	return new StringTokenIterator(target, delimiter, trim);
    }

    /**
     * Like tokenize, but skips escaped instances of the delimiter.
     *
     * @since 1.3.7
     */
    public static Iterator<String> tokenizeUnescaped(String target, String delimiter, boolean trim) {
	return new StringTokenIterator(target, delimiter, trim, true);
    }

    /**
     * Convert an Iterator of Strings to a List.
     *
     * @since 1.2
     */
    public static List<String> toList(Iterator<String> iter) {
	List<String> list = new ArrayList<String>();
	while (iter.hasNext()) {
	    list.add(iter.next());
	}
	return list;
    }

    /**
     * Wrap an Iterator in an Iterable.
     *
     * @since 1.3
     */
    public static Iterable<String> iterable(final Iterator<String> iterator) {
	return new Iterable<String>() {
	    public Iterator<String> iterator() {
		return iterator;
	    }
	};
    }

    /**
     * Strip quotes from a quoted String. If the string is not quoted, the original is returned.
     *
     * @since 1.3
     */
    public static String unquote(String s) {
	if (s.startsWith("\"") && s.endsWith("\"")) {
	    s = s.substring(1,s.length()-1);
	}
	return s;
    }

    /**
     * Check for ASCII values between [A-Z] or [a-z].
     *
     * @since 1.2
     */
    public static boolean isLetter(int c) {
	return (c >= 65 && c <= 90) || (c >= 97 && c <= 122);
    }

    /**
     * Check for ASCII values between [0-9].
     *
     * @since 1.2
     */
    public static boolean isNumber(int c) {
	return c >= 48 && c <= 57;
    }

    /**
     * Convert a char array to a byte array using UTF16 encoding.
     *
     * @since 1.2
     */
    public static byte[] toBytes(char[] chars) {
	return toBytes(chars, UTF16);
    }

    /**
     * Convert a char array to a byte array using the specified encoding. Like new String(chars).getBytes(charset), except without
     * allocating a String.
     *
     * @since 1.2
     */
    public static byte[] toBytes(char[] chars, Charset charset) {
	//
	// Perform the conversion
	//
	byte[] temp = charset.encode(CharBuffer.wrap(chars)).array();

	//
	// Terminate at the first NULL
	//
	int len = 0;
	for (int i=0; i < temp.length; i++) {
	    if (temp[i] == 0) {
		len = i;
		break;
	    } else {
		len++;
	    }
	}
	if (len == temp.length) {
	    return temp;
	} else {
	    byte[] trunc = Arrays.copyOfRange(temp, 0, len);
	    Arrays.fill(temp, (byte)0);
	    return trunc;
	}
    }

    /**
     * Convert a byte array in the specified encoding to a char array.
     *
     * @since 1.2
     */
    public static char[] toChars(byte[] bytes, Charset charset) {
	return toChars(bytes, 0, bytes.length, charset);
    }

    /**
     * Convert len bytes of the specified array in the specified encoding, starting from offset, to a char array.
     *
     * @since 1.2
     */
    public static char[] toChars(byte[] bytes, int offset, int len, Charset charset) {
	return charset.decode(ByteBuffer.wrap(bytes, offset, len)).array();
    }

    /**
     * Escape any regular expression elements in the string.  This is different from Pattern.quote, which simply puts the
     * string inside of \Q...\E.
     *
     * @since 1.2
     */
    public static String escapeRegex(String s) {
	return safeEscape(s, REGEX_STRS);
    }

    /**
     * Undo escapeRegex.
     *
     * @since 1.3.7
     */
    public static String unescapeRegex(String s) {
	return safeUnescape(s, REGEX_STRS);
    }

    /**
     * Returns true if the specified String contains any regular expression syntax.
     *
     * @since 1.2
     */
    public static boolean containsRegex(String s) {
	for (String ch : REGEX_STRS) {
	    if (s.indexOf(ch) != -1) {
		return true;
	    }
	}
	return false;
    }

    /**
     * @since 1.3.7
     */
    public static boolean isRegexChar(char c) {
	for (char ch : REGEX_CHARS) {
	    if (c == ch) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Returns true if the specified String contains any regular expression syntax that is not escaped.
     *
     * @since 1.2
     */
    public static boolean containsUnescapedRegex(String s) {
	for (int i=1; i < REGEX_STRS.length; i++) { // skip ESCAPE
	    int ptr = -1;
	    while ((ptr = s.indexOf(REGEX_STRS[i], ptr+1)) != -1) {
		if (!isEscaped(s, ptr)) {
		    return true;
		}
	    }
	}
	return false;
    }

    /**
     * Compiles a Perl-style regular expression with POSIX-style character classes into a Java regular expression.
     *
     * @since 1.2
     */
    public static Pattern pattern(String regex) throws PatternSyntaxException {
	return pattern(regex, 0);
    }

    /**
     * Compiles a Perl-style regular expression with POSIX-style character classes into a Java regular expression, with
     * the specified flags (from java.util.regex.Pattern).
     *
     * @since 1.2
     */
    public static Pattern pattern(String regex, int flags) throws PatternSyntaxException {
	return Pattern.compile(regexPosix2Java(regex), flags);
    }

    /**
     * Perform a substitution of POSIX character classes to Java character classes.
     *
     * @since 1.2
     */
    public static String regexPosix2Java(String pcre) {
	//
	// Escape all curly-brackets that are not:
	// 1) part of a Java character class
	// 2) part of a qualifier
	// 3) already escaped
	//
	StringBuffer sb = new StringBuffer();
	int start = 0;
	int next = pcre.indexOf(OPEN);
	if (next == -1) {
	    sb.append(escapeUnescaped(pcre, CLOSE));
	} else {
	    do {
		sb.append(escapeUnescaped(pcre.substring(start, next), CLOSE));
		if (isEscaped(pcre, next)) {
		    sb.append(OPEN);
		    start = next+1;
		} else {
		    int p2 = pcre.indexOf(CLOSE, next);
		    if (p2 == -1) {
			sb.append(escapeUnescaped(pcre.substring(next), OPEN));
			start = pcre.length();
		    } else {
			if (Pattern.matches(QUALIFIER_PATTERN, pcre.substring(next+1, p2))) {
			    // Qualifier
			    sb.append(pcre.substring(next, p2+1));
			    start = p2+1;
			} else if (next > 1 && !isEscaped(pcre,next-2) && pcre.substring(next-2,next).equals("\\p")) {
			    // Java character class
			    sb.append(pcre.substring(next, p2+1));
			    start = p2+1;
			} else {
			    sb.append("\\").append(OPEN);
			    start = next+1;
			}
		    } 
		} 
	    } while((next = pcre.indexOf(OPEN, start)) != -1);
	    sb.append(escapeUnescaped(pcre.substring(start), CLOSE));
	}
	String jcre = sb.toString();

	jcre = jcre.replace("[:digit:]", "\\p{Digit}");
	jcre = jcre.replace("[:alnum:]", "\\p{Alnum}");
	jcre = jcre.replace("[:alpha:]", "\\p{Alpha}");
	jcre = jcre.replace("[:blank:]", "\\p{Blank}");
	jcre = jcre.replace("[:xdigit:]","\\p{XDigit}");
	jcre = jcre.replace("[:punct:]", "\\p{Punct}");
	jcre = jcre.replace("[:print:]", "\\p{Print}");
	jcre = jcre.replace("[:space:]", "\\p{Space}");
	jcre = jcre.replace("[:graph:]", "\\p{Graph}");
	jcre = jcre.replace("[:upper:]", "\\p{Upper}");
	jcre = jcre.replace("[:lower:]", "\\p{Lower}");
	jcre = jcre.replace("[:cntrl:]", "\\p{Cntrl}");
	return jcre;
    }

    /**
     * Perform a substitution of POSIX character classes to Unicode character classes. Also, replaces '\_' with '_',
     * which is a harmless error in most regular expression engines, but not Microsoft's.
     *
     * @since 1.2
     */
    public static String regexPosix2Powershell(String pcre) {
	String psExpression = pcre;
	psExpression = psExpression.replace("[:digit:]", "\\d");
	psExpression = psExpression.replace("[:alnum:]", "\\p{L}\\p{Nd}");
	psExpression = psExpression.replace("[:alpha:]", "\\p{L}");
	psExpression = psExpression.replace("[:blank:]", "\\p{Zs}\\t");
	psExpression = psExpression.replace("[:xdigit:]","a-fA-F0-9");
	psExpression = psExpression.replace("[:punct:]", "\\p{P}");
	psExpression = psExpression.replace("[:print:]", "\\P{C}");
	psExpression = psExpression.replace("[:space:]", "\\s");
	psExpression = psExpression.replace("[:graph:]", "\\P{Z}\\P{C}");
	psExpression = psExpression.replace("[:upper:]", "\\p{Lu}");
	psExpression = psExpression.replace("[:lower:]", "\\p{Ll}");
	psExpression = psExpression.replace("[:cntrl:]", "\\p{Cc}");
	return safeUnescape(psExpression, "_");
    }

    /**
     * Attempt to convert a regex Pattern into a glob.
     *
     * @throws IllegalArgumentException if the pattern cannot be converted to a glob
     *
     * @since 1.2
     */
    public static String toGlob(Pattern p) throws IllegalArgumentException {
	String s = p.pattern();
	if (s.startsWith("^")) {
	    s = s.substring(1);
	} else if (!s.startsWith(".*")) {
	    throw new IllegalArgumentException();
	}
	if (s.endsWith("$")) {
	    s = s.substring(0, s.length()-1);
	} else if (!s.endsWith(".*")) {
	    s = new StringBuffer(s).append(".*").toString(); // trailing .* is implied
	}
	StringBuffer outerSb = new StringBuffer();
	Iterator<String> outerIter = tokenize(s, ".*", false);
	for (int i=0; outerIter.hasNext(); i++) {
	    if (i > 0) {
		outerSb.append("*");
	    }
	    String outerFrag = outerIter.next();
	    if (outerFrag.length() > 0) {
		StringBuffer innerSb = new StringBuffer();
		Iterator<String> innerIter = tokenize(outerFrag, "\\.", false);
		for (int j=0; innerIter.hasNext(); j++) {
		    if (j > 0) {
			innerSb.append(".");
		    }
		    String innerFrag = innerIter.next();
		    if (containsRegex(innerFrag)) {
			throw new IllegalArgumentException("contains regex: " + innerFrag);
		    } else {
			innerSb.append(innerFrag);
		    }
		}
		outerSb.append(innerSb.toString());
	    }
	}
	return outerSb.toString();
    }

    /**
     * Return the number of times ch occurs in target.
     *
     * @since 1.3
     */
    public static int countOccurrences(String target, char ch) {
	int count = 0;
	char[] chars = target.toCharArray();
	for (int i=0; i < chars.length; i++) {
	    if (chars[i] == ch) {
		count++;
	    }
	}
	return count;
    }

    /**
     * Read the contents of a File as a String, using the specified character set.
     *
     * @since 1.3.2
     */
    public static String readFile(File f, Charset charset) throws IOException {
	InputStreamReader reader = new InputStreamReader(new FileInputStream(f), charset);
	try {
	    StringBuffer buff = new StringBuffer();
	    char[] ch = new char[1024];
	    int len = 0;
	    while((len = reader.read(ch, 0, 1024)) > 0) {
		buff.append(ch, 0, len);
	    }
	    return buff.toString();
	} finally {
	    reader.close();
	}
    }

    /**
     * Determine whether or not the character at ptr is preceeded by an odd number of escape characters.
     *
     * @since 1.3.4
     */
    public static boolean isEscaped(String s, int ptr) {
	int escapes = 0;
	while (ptr-- > 0) {
	    if ('\\' == s.charAt(ptr)) {
		escapes++;
	    } else {
		break;
	    }
	}
	//
	// If the character is preceded by an even number of escapes, then it is unescaped.
	//
	if (escapes % 2 == 0) {
	    return false;
	}
	return true;
    }

    /**
     * Convert a Throwable stack trace to a String.
     *
     * @since 1.3.5
     */
    public static String toString(Throwable t) {
	StringBuffer sb = new StringBuffer(t.getClass().getName());
	sb.append(": ").append(t.getMessage() == null ? "null" : t.getMessage()).append(LF);
	StackTraceElement[] ste = t.getStackTrace();
	for (int i=0; i < ste.length; i++) {
	    sb.append("        at ").append(ste[i].toString()).append(LF);
	}
	Throwable cause = t.getCause();
	if (cause != null) {
	    sb.append("Caused by: ").append(toString(cause));
	}
	return sb.toString();
    }


    // Private

    /**
     * Comparator implementation for Strings.
     */
    private static final class StringComparator implements Comparator<String>, Serializable {
	boolean ascending = true;

	/**
	 * @param asc Set to true for ascending, false for descending.
	 */
	StringComparator(boolean asc) {
	    this.ascending = asc;
	}

	public int compare(String s1, String s2) {
	    if (ascending) {
		return s1.compareTo(s2);
	    } else {
		return s2.compareTo(s1);
	    }
	}

	public boolean equals(Object obj) {
	    return super.equals(obj);
	}
    }

    /**
     * Escape unescaped instances of the pattern in s.
     */
    private static String escapeUnescaped(String s, String pattern) {
	StringBuffer sb = new StringBuffer();
	int last = 0;
	int next = 0;
	while ((next = s.indexOf(pattern, last)) != -1) {
	    sb.append(s.substring(last, next));
	    if (isEscaped(s, next)) {
		sb.append(pattern);
	    } else {
		sb.append(ESCAPE).append(pattern);
	    }
	    last = next + pattern.length();
	}
	return sb.append(s.substring(last)).toString();
    }

    /**
     * Escape instances of the pattern in s which are not already escaped.
     */
    private static String safeEscape(String s, String... delims) {
	//
	// Insure ESCAPE is processed first
	//
	List<String> array = new ArrayList<String>(Arrays.<String>asList(delims));
	if (array.contains(ESCAPE) && !ESCAPE.equals(delims[0])) {
	    array.remove(ESCAPE);
	    List<String> temp = array;
	    array = new ArrayList<String>();
	    array.add(ESCAPE);
	    array.addAll(temp);
	    delims = array.<String>toArray(new String[array.size()]);
	}
	for (int i=0; i < delims.length; i++) {
	    String delim = delims[i];
	    List<String> list = toList(tokenize(s, delim, false));
	    StringBuffer escaped = new StringBuffer();
	    for (int j=0; j < list.size(); j++) {
		if (j > 0) {
		    escaped.append(ESCAPE);
		    escaped.append(delim);
		}
		escaped.append(list.get(j));
	    }
	    s = escaped.toString();
	}
	return s;
    }

    /**
     * Unescape the specified stack (of escaped delimiters) from the supplied String, s. Escaped
     * delimiters are unescaped in the order provided.
     */
    private static String safeUnescape(String s, String... delims) {
	//
	// Insure ESCAPE is processed last
	//
	List<String> array = new ArrayList<String>(Arrays.<String>asList(delims));
	int lastIndex = delims.length - 1;
	if (array.contains(ESCAPE) && !ESCAPE.equals(delims[lastIndex])) {
	    array.remove(ESCAPE);
	    List<String> temp = array;
	    array = new ArrayList<String>();
	    array.addAll(temp);
	    array.add(ESCAPE);
	    delims = array.<String>toArray(new String[array.size()]);
	}
	for (int i=0; i < delims.length; i++) {
	    String delim = ESCAPE + delims[i];
	    StringBuffer unescaped = new StringBuffer();
	    int last = 0;
	    int ptr = s.indexOf(delim);
	    while (ptr != -1) {
		unescaped.append(s.substring(last, ptr));
		if (isEscaped(s, ptr)) {
		    unescaped.append(delim);
		} else {
		    unescaped.append(delim.substring(1));
		}
		last = ptr + delim.length();
		ptr = s.indexOf(delim, last);
	    }
	    unescaped.append(s.substring(last));
	    s = unescaped.toString();
	}
	return s;
    }

    static final class StringTokenIterator implements Iterator<String> {
	private String target, delimiter, next, last=null;
	private boolean ignoreEscaped;
	int pointer;

	StringTokenIterator(String target, String delimiter) {
	    this(target, delimiter, true);
	}

	StringTokenIterator(String target, String delimiter, boolean trim) {
	    this(target, delimiter, trim, false);
	}

	StringTokenIterator(String target, String delimiter, boolean trim, boolean ignoreEscaped) {
	    if (trim) {
		//
		// Trim tokens from the beginning and end.
		//
		int len = delimiter.length();
		while(target.startsWith(delimiter)) {
		    target = target.substring(len);
		}
		while(target.endsWith(delimiter)) {
		    if (ignoreEscaped && isEscaped(target, target.length() - len)) {
			break;
		    } else {
			target = target.substring(0, target.length() - len);
		    }
		}
	    }
	    this.target = target;
	    this.delimiter = delimiter;
	    this.ignoreEscaped = ignoreEscaped;
	    pointer = 0;
	}

	public boolean hasNext() {
	    if (next == null) {
		try {
		    next = next();
		} catch (NoSuchElementException e) {
		    return false;
		}
	    }
	    return true;
	}

	public String next() throws NoSuchElementException {
	    if (next != null) {
		String tmp = next;
		next = null;
		return tmp;
	    }
	    int i = pointer;
	    do {
		if (i > pointer) {
		    i += delimiter.length();
		}
		i = target.indexOf(delimiter, i);
	    } while (i != -1 && ignoreEscaped && isEscaped(target, i));
	    if (last != null) {
		String tmp = last;
		last = null;
		return tmp;
	    } else if (pointer >= target.length()) {
		throw new NoSuchElementException("No tokens after " + pointer);
	    } else if (i == -1) {
		String tmp = target.substring(pointer);
		pointer = target.length();
		return tmp;
	    } else {
		String tmp = target.substring(pointer, i);
		pointer = (i + delimiter.length());
		if (pointer == target.length()) {
		    // special case; append an empty token when ending with the token
		    last = "";
		}
		return tmp;
	    }
	}

	public void remove() {
	    throw new UnsupportedOperationException("Remove not supported");
	}
    }
}
