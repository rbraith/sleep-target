package com.rbraithwaite.sleepapp.ui.format;

import com.rbraithwaite.sleepapp.ui.Constants;

import java.util.Locale;
import java.util.regex.Pattern;

public class DurationFormatter
{
//*********************************************************
// private properties
//*********************************************************

    private String mFormat;
    private Locale mLocale;

//*********************************************************
// private constants
//*********************************************************

    private static final Pattern formatPattern = Pattern.compile(".*%\\d*d.*%\\d*d.*%\\d*d");


//*********************************************************
// constructors
//*********************************************************

    
    /**
     * Class for various sleep session duration formatting operations.
     *
     * @param format The format for the duration, should have 3 integer (%d) fields, corresponding
     *               to hours, minutes, and seconds.
     * @param locale The locale for the format.
     */
    public DurationFormatter(Locale locale, String format)
    {
        validateFormat(format);
        
        mFormat = format;
        mLocale = locale;
    }
    
    /**
     * Uses the standard values for the locale and format
     */
    public DurationFormatter()
    {
        mFormat = Constants.STANDARD_FORMAT_DURATION;
        mLocale = Constants.STANDARD_LOCALE;
    }


//*********************************************************
// api
//*********************************************************

    
    /**
     * Formats the provided millisecond duration into the form: "Hh MMm SSs"
     *
     * @param durationMillis The duration to format, in milliseconds. Must be >= 0.
     *
     * @return The formatted duration String.
     */
    public String formatDurationMillis(long durationMillis)
    {
        if (durationMillis < 0) {
            throw new IllegalArgumentException(String.format("duration must be >= 0 (%d)",
                                                             durationMillis));
        }
        
        long durationAsSeconds = durationMillis / 1000L;
        
        long durationAsMinutes = durationAsSeconds / 60;
        long seconds = durationAsSeconds % 60;
        long minutes = durationAsMinutes % 60;
        long hours = durationAsMinutes / 60;
        
        return String.format(mLocale, mFormat, hours, minutes, seconds);
    }


//*********************************************************
// private methods
//*********************************************************

    
    /**
     * just check for any exceptions thrown by String.format()
     */
    // REFACTOR [20-11-15 6:16PM] -- make this public?
    private void validateFormat(String format)
    {
        if (!formatPattern.matcher(format).matches()) {
            throw new IllegalArgumentException(String.format(
                    "Invalid format (%s), expected 3 integer fields (%%d)",
                    format));
        }
    }
}
