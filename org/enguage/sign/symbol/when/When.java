package org.enguage.sign.symbol.when;

import java.util.ListIterator;

import org.enguage.util.attr.Attributes;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;
/*
import java.util.ArrayList;
class Strings extends ArrayList<String> {
	public Strings() {}
	public Strings( String s ) {
		int i=0;
		while( i<s.length()) {
			while (i<s.length() && s.charAt( i ) == ' ') i++;
			if (i<s.length()) {
				String tmp = "";
				while (i<s.length() && s.charAt( i ) != ' ')
					tmp += s.charAt( i++ );
				add( tmp );
	}	}	}
	public Strings contract( String s ) { return this; }
}
// */
public class When {
	public static final String NAME   = "when";
	public static final String ID     = "WHEN";
	public static final String MO_SEP = "/";
	public static final char   mo_sep = MO_SEP.charAt( 0 );
	
	// types - used in constructing When (FROM implies TO also)
	public static final int   AT = 0; // inital type - a moment
	public static final int FROM = 1;
	public static final int   TO = 2;
	
	public static       Audit audit = new Audit( NAME );

	// general properties to help constuct Whens
	private static boolean future  = false;
	private static boolean past    = false;
	private static boolean present = true;
	public  static void    pastIs()    { past    = true; present = future  = false; }
	public  static void    futureIs()  { future  = true; past    = present = false; }
	public  static void    presentIs() { present = true; future  = past    = false; }
	public  static boolean isPast()    { return past; }
	public  static boolean isFuture()  { return future; }
	public  static boolean isPresent() { return present; }

	//constructors
	public When() { scale( Time.DAY );	}
	public When( Moment m ) { this( m, m ); }
	public When( String s ) { // s = output from valueOf(), e.g. "19630818073088-20480905070088"
		Strings sa = new Strings( s, mo_sep ); // s.size() == 2 -> from/to
		int size = sa.size();
		if (size==1)
			to = from = new Moment( sa.get( 0 ));
		else if (size == 2) {
			from = new Moment( sa.get( 0 ));
			to   = new Moment( sa.get( 1 ));
	}	}
	public When( Moment fr, Moment t ) { from = fr; to = t; }
	
	// from/to Attributes
	public When( Attributes a ) { this( a.value( "when" )); }
	public Attributes toAttributes() { return new Attributes().add( "when", valueOf());}
	
	public static boolean valid( String s ) {
		if (s != null && s.length() == 29) {
			for (char ch : s.toCharArray() )
				if (!Character.isDigit( ch ) && ch != mo_sep)
					return false;
			return true;
		}
		return false;
	}
	
	// helper ("non-static factory") methods
	public When today() {
		//audit.in( "today", "w="+ toString());
		long today = (Moment.getNow().moment() / Time.DAY ); // * Moment.DAY;
		
		if (type == TO) to().date( today ); else from().date( today );
		
		scale( Time.DAY );
		//audit.out( toString());
		return this;
	}
	public When yesterday() { return today().prevDay();}
	public When tomorrow()  { return today().nextDay();}
	public When thisWeek( String requiredDay ) {
		//audit.in( "thisWeek", "w="+ toString() +", req="+ requiredDay );
		today();
		int actualDoW = type == TO ?
				Day.dayInWeek( Day.name( to().moment() )) :
				Day.dayInWeek( Day.name( from().moment() ));
		int reqireDoW = Day.dayInWeek( requiredDay );
		dayShift( reqireDoW - actualDoW );
		shift();
		//audit.out( toString());
		return this;
	}
	// apply
	public When anniversary( int date ) {
		if (type==TO) to.anniversary( date ); else from.anniversary( date );
		return this;
	}
	public When christmas() { return today().anniversary( 1225 ); }
	public When christmas( int year ) { return today().anniversary(  year * 10000 + 1225 ); }

	
	// members
	private Moment now = Moment.getNow();
	public	long   now() { return now.moment(); }
	
	private Moment from = new Moment();
	public  Moment from() { return from; }
	public	When   from( Moment m ) { from = m; type = FROM; return this; }
	
	private Moment to = from;
	public	Moment to() { return to; }
	public	When   to( Moment m ) { to = m; type = TO; return this; }

	private int  type = AT; // AT, AT->FROM, AT->TO, AT->FROM->TO, AT->TO->FROM.
	public	int  type() { return type; }
	public	When type( int t ) {
		if (type==AT && (t==TO || t==FROM)) // split!
			to = new Moment( to );
		type = t;
		return this;
	}

	private boolean absolute = true;
	public	boolean isAbsolute() { return absolute; }
	public	When	absoluteIs( boolean b ) { absolute =  b; return this;}
	public	boolean isRelative() { return !absolute; }
	public	When	relativeIs( boolean b ) { absolute = !b; return this;}
	
	private boolean spurious = false; // 35th May -- a real date!
	public  When    spurious(boolean b) { spurious = b; return this; }
	public  boolean spurious() {return spurious; }

	private boolean exact = false; // until seconds given or "precisely" or "exactly"
	public	boolean isExact() { return exact; }
	public	When	exactIs( boolean b ) { exact = b; return this; }

	public	long scale() { return type == TO ? to.scale() : from.scale(); }
	public	When scale( long l ) { if (type == TO) to().scale( l ); else from().scale( l ); return this; }
	public static String scaleToString( long scale ) {
		     if (scale == Time.YEAR)  return "year";
		else if (scale == Time.MONTH) return "month";
		else if (scale == Time.DAY)   return "day";
		else if (scale == Time.HOUR)  return "hour";
		else if (scale == Time.MINUTE)return "minute";
		else /* if (scale == S) */    return "second";
	}
	
	/* ++++++++++++++++++++++++++++++++++++++++++++++++++
	 * shifting is a transformation on a time point to be 
	 * applied once the timepoint is determined. E.g.
	 * "a week ago last Thursday" is 
	 * "a week ago" a -7 day shift on, wait for it, "last Thursday"
	 */
	private long dayShift = 0;
	public  void dayShift( long l ) { audit.debug("ds="+l); dayShift += l; }
	
	private long monthShift = 0;
	public  void monthShift( long l ) { audit.debug("ms="+l); monthShift += l;}
	
	private long yearShift = 0;
	public  void yearShift( long l ) { audit.debug("ys="+l); yearShift += l; }
	
	public When shift() {
		//audit.in( "shift", toString() +", y="+ yearShift +", m="+monthShift +", d="+ dayShift );
		
		if (isUnassigned()) today();
		
		// shift when...
		if (yearShift < 0)  while ( yearShift++ != 0) prevYear();
		               else while ( yearShift-- != 0) nextYear();
		if (monthShift < 0) while (monthShift++ != 0) prevMonth();
		               else while (monthShift-- != 0) nextMonth();
		if (dayShift < 0)   while (  dayShift++ != 0) prevDay();
		               else while (  dayShift-- != 0) nextDay();
		// ... and, reset shifts
		dayShift = monthShift = yearShift = 0;
		//audit.out( toString() );
		return this;
	}
	/* ++++++++++++++++++++++++++++++++++++++++++++++++ */

	// methods
	public void year( long y ) { if (type == TO) to().year(  y ); else from().year(  y );}
	public void month( int m ) { if (type == TO) to().month( m ); else from().month( m );}
	public void day(   int d ) { if (type == TO) to().day(   d ); else from().day(   d );}
	public void hour(  int h ) { if (type == TO) to().hour(  h ); else from().hour(  h );}
	public void minute(int m ) { if (type == TO) to().minute(m ); else from().minute(m );}
	public void second(int s ) { if (type == TO) to().second(s ); else from().second(s );}
	
	public long year(  ) { return (type == TO)? to().year()   : from().year();  }
	public int  month( ) { return (type == TO)? to().month()  : from().month(); }
	public int  day(   ) { return (type == TO)? to().day()    : from().day();   }
	public int  hour(  ) { return (type == TO)? to().hour()   : from().hour();  }
	public int  minute() { return (type == TO)? to().minute() : from().minute();}
	public int  second() { return (type == TO)? to().second() : from().second();}
	
	private When nextYear() {if (type==TO) to.nextYear(); else from.nextYear(); return this; }
	private When prevYear() {if (type==TO) to.prevYear(); else from.prevYear(); return this; }
	private When nextMonth(){if (type==TO) to.nextMonth();else from.nextMonth();return this; }
	private When prevMonth(){if (type==TO) to.prevMonth();else from.prevMonth();return this; }
	private When nextDay()  {if (type==TO) to.nextDay();  else from.nextDay();  return this; }
	private When prevDay()  {if (type==TO) to.prevDay();  else from.prevDay();  return this; }

	public boolean isUnassigned() {return from.isUnassigned() && to.isUnassigned();}
	public void    unassign() { from.unassign(); to = from; type( AT ); }
	
	public When am() {if (type == TO) to().am( true ); else from().am( true ); return this;}
	public When pm() {if (type == TO) to().pm( true ); else from().pm( true ); return this; }
	public When time(long time ) {if (type == TO) to().time( time ); else from().time( time ); return this; }
	public long time() {return type == TO ? to.time() : from.time();}
	public When date( long d ) {if (type == TO) to().date( d ); else from().date( d ); return this; }
	public long date( int t ) {return t == TO ? to.time() : from.time();}
	public long date() {return type == TO ? to.time() : from.time();}
	
	boolean isDuration() { return  (from != to) && !from.isUnassigned() && !to.isUnassigned(); }
	boolean isMoment()	{  return  from == to; }
	

	// attributes -- as per Number.java
	public	When representamen( String  s ) {
		if (type == TO)
			to.representamen( s );
		else
			from.representamen( s );
		return this;
	}
	public	When	representamen( Strings s ) {
		if (type == TO)
			to.representamen( s );
		else
			from.representamen( s );
		return this;
	}
	public When representamenAppend( Strings s ) {
		if (type == TO)
			to.representamen().addAll( s );
		else
			from.representamen().addAll( s );
		return this;
	}
	public	Strings representamen() {return type == TO ? to.representamen() : from.representamen();}
	
	//--
	
	// Let's call digital time "10:30"
	// represented as [ "10", ":", "30" ]
	// with an absolute value of 00000000103000
	// so "today at 10:30" will be yyyymmdd103000

	/*
	 * When -						 
	 *	 At 7:30					| now | ([RQUAL]today|yesterday|tomorrow |[ [DQUAL] DAY|week|year ] [at TIME])
	 *	 tomorrow		  |	future  |	  | in [ TDUR ]
	 *	 tomorrow at 7:30 |	future  | 
	 *	 now			  | present | where::
	 *	 today            |         | TDUR = half an hour
	 *	 last Wednesday	  |	 past   | TIME = 10:05 | [a] quarter to ten
	 *	 this Wednesday	  |?closest?| DQUAL = this | last
	 *	 on Friday					| RQUAL = the day (before|after)
	 *	 on the 27th       ?context?| 
	 *	 ----------------------------------------------------------------------------------------------------------
	 *	 in ten minutes
	 */
	
	private String rep = "";
	public  String rep() { return rep; }
	public  When   rep( String s ) { rep = s; return this;}
	public  When   rep( Strings s ) { return rep( s.toString());}
	
	public String toString() {
		String rc = isMoment() ? from.toString( "" ) : toString( "from", "until" );
		return rc.equals( "" ) ? rep() : rc;
	}
	public String toString( String fromPrefix, String toPrefix ) {
		String rc = "";
		if (!from.isUnassigned()) rc += from.toString( fromPrefix );
		if (  !to.isUnassigned()) rc +=   to.toString(   toPrefix );
		return rc.equals( "" ) ? rep() : rc;
	}
	public String valueOf() {
		String rc = from.valueOf();
		if (from.moment() != to.moment()) rc += MO_SEP + to.valueOf();
		return rc;
	}

	private static boolean interpret( When w, ListIterator<String> si ) {
		//audit.in( "interpret", "" );
		boolean rc = false; // if false the caller will rewind si
		w.type( AT );
		if ((Strings.doString( "on", si ) && !(rc = Relative.doOn( w, si ))) ||
		    (Strings.doString( "at", si ) && !(rc = Time.doAt(     w, si ))) ||
			(Strings.doString( "in", si ) && !(rc = Relative.doIn( w, si )))   ) // do [month] year
			si.previous(); // replace "in", "at" or "on"
		
		if (Relative.doRelativeDay( w, si ))
			rc = true; // ignore failure, doRelativeDay() is a bonus!
		// -- */
		// not quite the same as above, or... "at Tuesday" would be fine!
		/* if (  (Strings.doString( "in", si )
		 * 	|| Strings.doString( "on", si )
		 * 	|| Strings.doString( "at", si ))
		 * 	&& !(rc = Moment.doMoment( w.type( AT ), si )))
		 * 		si.previous();
		 */
		if (  (Strings.doString( "from", si )
			|| Strings.doString( "after", si ))
			&& !(rc = Moment.doMoment( w.type( FROM ), si )))
				si.previous(); // replace from/after
		
		
		if ((   Strings.doString(  "until", si )
			 || Strings.doString(     "to", si )
			 || Strings.doString( "before", si ))
			 && !(rc = Moment.doMoment( w.type( TO ), si )))
				si.previous(); // replace until/to
	
		return rc; // audit.out( rc );
	}
	// --
	public static When getWhen( Strings sa ) { return getWhen( new When(), sa ); }
	public static When getWhen( When w, Strings sa ) {
		if (sa != null) {
			Audit.suspend();
			w.type( AT );
			int start = 0;
			boolean found = false;
			ListIterator<String> si = sa.listIterator();
			while (!found && si.hasNext()) {
				start = si.nextIndex();
				found = When.interpret( w, si );
				if (!found && si.hasNext())
					si.next();
			}
			if (found)
				w.shift().representamen( sa.remove( start, si.nextIndex() - start ));
			else
				w.unassign();
			
			Audit.resume();
		}
		return w;
	}
	// -- test code
	private static void testGet( String s ) { testGet( s, s ); } // default to true
	private static void testGet( String request, String result ) {
		Strings sa = new Strings( request );
		When w = When.getWhen( new When(), sa );
		if (w == null)
			audit.FATAL( "w is null!" );
		else {
			String ws = w.toString();
			boolean shouldBeAssigned = !request.equals( result );
			audit.debug( "'"+ request  +"' means"+ (!w.isUnassigned() || shouldBeAssigned? ":" : " just that!") );
			if (!w.isUnassigned() || shouldBeAssigned) audit.debug( "'"+ (result.equals("") ? sa.toString( Strings.SPACED ):result) +"' (when="+ ws +")" );
			if (shouldBeAssigned && w.isUnassigned()) audit.FATAL( "w is unassigned!! >"+ w.toString() +"<" );
			if (!shouldBeAssigned && !w.isUnassigned()) audit.FATAL( "w is assigned!" );
			if (!result.equals("") && !result.equals( sa.toString( Strings.SPACED ))) audit.FATAL( "wrong answer: "+ sa.toString( Strings.SPACED ) +"!" );
			audit.debug( "" );
	}	}
	public static void main( String args[]) {
		When w = new When( Moment.getNow());
		audit.debug( "Now is "+ w.toString());
		audit.debug( "Today is "+ w.today().toString());
		audit.debug( "Wednesday is "+ w.thisWeek( Day.WED ).toString());
		audit.debug( "how long is it to xmas? "+ new Duration( new When().now(), w.christmas().from().moment() ).toString());
		audit.debug( "Christmas day is on a "+w. christmas().from.dayName()); // was at
		testGet( "now i am meeting my brother" );
		
		int test = 0;
		if (test==1 || test==0) {
			// a from/until/on the DATE|DAY [at TIME]
			audit.debug( "test 1 ********************" );
			testGet( "i was born on the sabbath" );
			Audit.on();
			//Audit.off(); Audit.traceAll( false );
			testGet( "i was born on the 18th of August 1964",            "i was born" );
			testGet( "i was born on the 18th of August 1964 at 10:30am", "i was born" );
			testGet( "i met my brother on holiday" );
			testGet( "I am meeting emily at 7:30", "I am meeting emily");
			testGet( "I am meeting james tomorrow at 7:30", "I am meeting james");
			testGet( "i met my brother on Thursday", "i met my brother" );
			testGet( "i met my brother on Thursday at 6pm", "i met my brother" );
			audit.debug( "test 1 passes!" );
		}
		if (test == 2 || test == 0) {
			audit.debug("test 2 ********** this,next,last **********");
			testGet( "i am meeting my brother for the last time", "i am meeting my brother for the last time" );
			testGet( "i am meeting my brother last Thursday", "i am meeting my brother" );
			testGet( "i am meeting my brother this way" );
			testGet( "i am meeting my brother this Thursday", "i am meeting my brother" );
			testGet( "i am meeting my brother next time" );
			testGet( "i am meeting my brother next Thursday", "i am meeting my brother" );
			audit.debug( "test 2 passes!" );
		}
		if (test == 3 || test == 0) {
			audit.debug("test 3 ********** yesterday, today, tomorrow **********");
			testGet( "i met my brother yesterday", "i met my brother" );
			testGet( "i am meeting my brother today", "i am meeting my brother" );
			testGet( "i am reading today's menu" );
			testGet( "i am meeting my brother tomorrow", "i am meeting my brother" );
			audit.debug( "test 3 passes!" );
		}
		if (test == 4 || test == 0) {
			audit.debug( "test 4 ********** a week ago **********" );
			testGet( "i met my brother a week ago last way" );
			testGet( "i met my brother a week ago last Thursday", "i met my brother" );
			testGet( "i am meeting my brother a week ago", "i am meeting my brother" );
			testGet( "i am meeting my brother a week ago last Thursday", "i am meeting my brother" );
			testGet( "i am meeting my brother a week last Thursday", "i am meeting my brother" );
			testGet( "I met my brother 2 months ago at the pub", "I met my brother at the pub" );
			testGet( "I met my brother 2 months ago yesterday at the pub", "I met my brother at the pub" );
			testGet( "I met my brother a year ago tomorrow at the pub", "I met my brother at the pub" );
			audit.debug( "test 4 passes!" );
		}
		if (test == 5 || test == 0) {
			audit.debug("test 5 ********* until ***********");
			//Audit.on(); Audit.traceAll( true );
			testGet( "I am with my brother until 7:30pm tomorrow at the pub", "I am with my brother at the pub" );
			testGet( "we are going to from Wednesday until today", "we are going to" );
			testGet( "we are going to from Wednesday until last night", "we are going to" );
			testGet( "we are going to from Wednesday until Thursday", "we are going to" );
			testGet( "we are going to from Wednesday until the 11th of February 2016", "we are going to" );
			testGet( "we are going to from Wednesday until Thursday the 11th of February 2016", "we are going to" );
			testGet( "Queen Elizabeth I reigned from 1558 to 1603", "Queen Elizabeth I reigned");
			testGet( "we are going to from 1558 to 1603", "we are going to");
			audit.debug( "test 5 passes!" );
		}
		if (test == 6 || test == 0) {
			audit.debug("test 6 ********* simple tense context ***********");
			audit.debug( "setting past" );
			When.pastIs();
			testGet( "i met my brother on Thursday", "i met my brother" );
			audit.debug( "setting present" );
			When.presentIs();
			testGet( "i am meeting my brother on Thursday", "i am meeting my brother" );
			audit.debug( "setting future" );
			When.futureIs();
			testGet( "i am meeting my brother on Thursday", "i am meeting my brother" );
			audit.debug( "test 6 passes!" );
		}
		audit.debug( ">>>:"+ new When().rep( "idk" ).toString() +":" );
/*
		testGet( "i am meeting my brother now", null, "a moment in time" ); // don't check time - it changes!
		testGet( "i am meeting my brother today", "today", "a day" );
		testGet( "i am meeting my brother tomorrow", "tomorrow", "a day" );
		testGet( "i am meeting my brother yesterday", "yesterday", "a day" );
		testGet( "i am meeting my brother at 5pm",    "at 5 pm", "a moment in time" );
		testGet( "i am meeting my brother at 7:30",   "at 7 30", "a moment in time" );
		testGet( "i am meeting my brother at 7:30 pm",   "at 7 30 pm", "a moment in time" );
		testGet( "i am meeting my brother at 7:30 am", "at 7 30 am", "a moment in time" );
		testGet( "i am meeting my brother 7:30:42",    "at 7 30 and 42 seconds", "a moment in time" );
		testGet( "i am meeting my brother on Thursday",   null, "a day" ); // this is probably wrong - d/t is not known
		testGet( "i am meeting my brother next Thursday", null, "a day" ); // this is probably wrong - d/t is not known
		// --*/
		//testGet( "i met my brother at 5pm", null, "a moment in time" ); // this is probably wrong - d/t is not known
		// -- testGet( "a week ago last Thursday" );
		// -- testGet( "a week tomorrow" );
		//w = When.day( new Strings( "1225" ));
		//if (null != w)
		//	audit.audit( "Day of week is "+ Moment.dayOfWeek( w.from()));
		//w = When.day( new Strings( "the 25th December" ));
		//if (null != w)
		//	audit.audit( "Day of week is "+ When.Moment.dayOfWeek( w.from()));
		//testGet( "I'm meeting my brother at 7:30 at the pub", "at 7 30", "a moment in time" );
		//audit.audit( new When( Long.valueOf( 73000 ) ).toString());
}	}
