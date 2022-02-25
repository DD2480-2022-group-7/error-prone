Assigning a Throwable to a static field is highly indicative of a bug, or
error-prone design. If such a Throwable were to be thrown, then the stack trace
and/or causal chain would likely be incorrect at run time.

Common reasons are:

1.  The field simply isn't needed, and there's a bug.

2.  An attempt is being made to cache or reuse a Throwable (often, a particular
    Exception). In this case, consider whether this is really is necessary: it
    often isn't. Could a Throwable simply be instantiated when needed?

3.  If reuse is strictly necessary, it is very hairy: the static field could
    potentially be accessed from a static method even before the class is
    initialized, or before this particular assignment becomes visible to another
    thread. Refer to e.g.
    [JLS §12.4.2 "Detailed Initialization Procedure"](https://docs.oracle.com/javase/specs/jls/se8/html/jls-12.html#jls-12.4.2)
    and
    [JLS §17.4.5 "Happens-before Order"](https://docs.oracle.com/javase/specs/jls/se8/html/jls-17.html).
