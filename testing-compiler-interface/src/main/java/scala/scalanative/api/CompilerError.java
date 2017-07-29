package scala.scalanative.api;

public interface CompilerError {

    /**
     * Returns the point of the position where the error is reported.
     * Note a single point is returned even if range positions are enabled.
     *
     * @return point of the position
     */
    public Integer getPosition();

    /**
     * Returns the message of the error, as reported by the compiler.
     *
     * @return String value representing the error message
     */
    public String getErrorMsg();

}
