package mchorse.mappet.api.scripts.user.mappet;

import java.util.Set;

/**
 * This interface represents Mappet states. Server ({@link mchorse.mappet.api.scripts.user.IScriptServer}),
 * players ({@link mchorse.mappet.api.scripts.user.entities.IScriptPlayer})
 * and NPCs ({@link mchorse.mappet.api.scripts.user.entities.IScriptNpc}) can have states.
 *
 * <pre>{@code
 *    function main(c)
 *    {
 *        var states = c.getServer().getStates();
 *
 *        // Do something with global states...
 *    }
 * }</pre>
 */
public interface IMappetStates
{
    /**
     * Add some value to existing state by ID.
     *
     * <pre>{@code
     *    var states = c.getServer().getStates();
     *
     *    states.add("total_spending", 20);
     *    c.send("Total spending is now " + states.getNumber("total_spending"));
     * }</pre>
     *
     * @return original value plus the provided value
     */
     double add(String id, double value);

    String add(String id, String value);

    /**
     * Set numeric value to existing state by ID.
     *
     * <pre>{@code
     *    var states = c.getServer().getStates();
     *
     *    states.setNumber("total_spending", 1000000001);
     *    c.send("Total spending is now " + states.getNumber("total_spending"));
     * }</pre>
     */
     void setNumber(String id, double value);

    /**
     * Set string value to existing state by ID.
     *
     * <pre>{@code
     *    var states = c.getSubject().getStates();
     *
     *    states.setString("name", "Jeff");
     *    c.getSubject().send("Your name is " + states.getString("name"));
     * }</pre>
     */
     void setString(String id, String value);

    void setBoolean(String id, boolean value);

    boolean toggleBoolean(String id);

    /**
     * Get a numeric value of a state by given ID.
     *
     * <pre>{@code
     *    var states = c.getServer().getStates();
     *
     *    c.send("Total spending is " + states.getNumber("total_spending"));
     * }</pre>
     *
     * @return state value, or 0 if no state found
     */
     double getNumber(String id);

    /**
     * Check if a state instance of number.
     *
     * <pre>{@code
     *    var states = c.getServer().getStates();
     *
     *    c.send("State is number: " + states.isNumber("state_number"));
     * }</pre>
     */
     boolean isNumber(String id);

    /**
     * Get a string value of a state by given ID.
     *
     * <pre>{@code
     *    var states = c.getSubject().getStates();
     *
     *    c.send("Your RPG class is: " + states.getString("class"));
     * }</pre>
     *
     * @return state value, or empty string if no state found
     */
     String getString(String id);

    /**
     * Check if a state instance of string.
     *
     * <pre>{@code
     *    var states = c.getServer().getStates();
     *
     *    c.send("State is string: " + states.isString("state_string"));
     * }</pre>
     */
     boolean isString(String id);

    boolean getBoolean(String id);

    boolean isBoolean(String id);

    /**
     * Removes a state by given ID.
     *
     * <pre>{@code
     *    var states = c.getServer().getStates();
     *
     *    // The city has been defaulted
     *    states.reset("total_spendings");
     * }</pre>
     */
     void reset(String id);

    /**
     * Removes multiple states by using mask.
     *
     * <pre>{@code
     *    var states = c.getServer().getStates();
     *
     *    // Remove all states that start with "regions."
     *    states.resetMasked("regions.*");
     * }</pre>
     */
     void resetMasked(String id);

    /**
     * Remove all states.
     *
     * <pre>{@code
     *    var states = c.getServer().getStates();
     *
     *    // Game over
     *    states.clear();
     * }</pre>
     */
     void clear();

    /**
     * Check whether state by given ID exists.
     *
     * <pre>{@code
     *    var states = c.getSubject().getStates();
     *    var name = states.has("name") ? states.getString("name") : "Jeff";
     *
     *    c.getSubject().send("Your name is " + name);
     * }</pre>
     */
     boolean has(String id);

    /**
     * Get IDs of all states.
     *
     * <pre>{@code
     *    var states = c.getSubject().getStates().keys();
     *
     *    for each (var key in states)
     *    {
     *        c.send("Server has state: " + key);
     *    }
     * }</pre>
     */
     Set<String> keys();
}