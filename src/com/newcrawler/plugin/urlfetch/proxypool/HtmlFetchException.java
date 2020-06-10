package com.newcrawler.plugin.urlfetch.proxypool;


public class HtmlFetchException extends RuntimeException
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -3489543824342888514L;

	/**
	 * 
	 */

	/** Array of nested Throwables (optional) */
    Throwable[] nested;

    /** The object being processed when the error was encountered (optional). */
    Object failed;

    /** Flag defining if this exception is fatal, or could be retried with the possibility of success. */
    boolean fatal;

    /**
     * Constructs a new exception without a detail message.
     */
    public HtmlFetchException()
    {
    }

    /**
     * Constructs a new exception with the specified detail message.
     * @param msg the detail message.
     */
    public HtmlFetchException(String msg)
    {
        super(msg);
    }

    /**
     * Constructs a new exception with the specified detail message and nested <code>Throwable</code>s.
     * @param msg the detail message.
     * @param nested the nested <code>Throwable[]</code>.
     */
    public HtmlFetchException(String msg, Throwable[] nested)
    {
        super(msg);
        this.nested = nested;
    }
    public HtmlFetchException( Throwable nested)
    {
        super(nested);
        this.nested = new Throwable[]{nested};
    }

    /**
     * Constructs a new exception with the specified detail message and nested <code>Throwable</code>.
     * @param msg the detail message.
     * @param nested the nested <code>Throwable</code>.
     */
    public HtmlFetchException(String msg, Throwable nested)
    {
        super(msg);
        this.nested = new Throwable[]{nested};
    }

    /**
     * Constructs a new exception with the specified detail message and failed object.
     * @param msg the detail message.
     * @param failed the failed object.
     */
    public HtmlFetchException(String msg, Object failed)
    {
        super(msg);
        this.failed = failed;
    }

    /**
     * Constructs a new exception with the specified detail
     * message, nested <code>Throwable</code>s, and failed object.
     * @param msg the detail message.
     * @param nested the nested <code>Throwable[]</code>.
     * @param failed the failed object.
     */
    public HtmlFetchException(String msg, Throwable[] nested, Object failed)
    {
        super(msg);
        this.nested = nested;
        this.failed = failed;
    }

    /**
     * Constructs a new exception with the specified detail message, nested <code>Throwable</code>,
     * and failed object.
     * @param msg the detail message.
     * @param nested the nested <code>Throwable</code>.
     * @param failed the failed object.
     */
    public HtmlFetchException(String msg, Throwable nested, Object failed)
    {
        super(msg);
        this.nested = new Throwable[]{nested};
        this.failed = failed;
    }

    /**
     * Method to set the exception as being fatal.
     * Returns the exception so that user code can call
     * "throw new JPOXException(...).setFatal();"
     * @return This exception (for convenience)
     */
    public HtmlFetchException setFatal()
    {
        fatal = true;
        return this;
    }

    /**
     * Accessor for whether the exception is fatal, or retriable.
     * @return Whether it is fatal
     */
    public boolean isFatal()
    {
        return fatal;
    }

    /**
     * The exception may include a failed object.
     * @return the failed object.
     */
    public Object getFailedObject()
    {
        return failed;
    }

    /**
     * The exception may have been caused by multiple exceptions in the runtime.
     * If multiple objects caused the problem, each failed object will have its
     * own <code>Exception</code>.
     * @return the nested Throwable array.
     */
    public Throwable[] getNestedExceptions()
    {
        return nested;
    }

    /**
     * Return the first nested exception (if any), otherwise null.
     * @return the first or only nested Throwable.
     */
    public synchronized Throwable getCause()
    {
        return ((nested == null || nested.length == 0) ? null : nested[0]);
    }
    
}