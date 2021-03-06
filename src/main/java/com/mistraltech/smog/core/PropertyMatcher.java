package com.mistraltech.smog.core;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * A wrapper for a Hamcrest matcher that matches a property of the target object. Knows the name
 * of the property being matched and helps with the mismatch description if the match fails.
 * <p>
 * Example: Assume a Person class has an Address property and that PersonMatcher is a {@link CompositePropertyMatcher}
 * class for matching against Person. PersonMatcher declares a PropertyMatcher&lt;Address&gt; field that can be assigned a
 * matcher for the Address property. If no matcher is assigned to a PropertyMatcher when the {@link #matches(Object)}
 * method is invoked, the match is assumed to have succeeded.
 *
 * @param <T> the type of the property being matched
 */
public class PropertyMatcher<T> extends BaseMatcher<T> implements PathProvider {
    /**
     * A contained matcher for matching the property that this PropertyMatcher represents in
     * the target object.
     * <p>
     * May be null, in which case {@link #matches(Object)} will automatically succeed.
     */
    private Matcher<? super T> matcher;

    /**
     * The name of the property in the target object that this PropertyMatcher represents
     * and matches against.
     */
    private String propertyName;

    /**
     * Tells us where the property represented by this PropertyMatcher
     * exists in the object graph, relative to the root object being matched.
     * <p>
     * E.g. if this PropertyMatcher represents the houseNumber property on an Address object, the
     * path might be "person.address" or "company.address".
     */
    private PathProvider pathProvider;

    /**
     * Constructor that takes a PropertyMatcherRegistry. This instance will register itself with the registry.
     *
     * @param propertyName name of the attribute that this PropertyMatcher matches against in the target object
     * @param registry the PropertyMatcherRegistry to register with; can be null
     */
    public PropertyMatcher(String propertyName, PropertyMatcherRegistry registry) {
        this(propertyName, registry, null);
    }

    /**
     * Constructor that takes and assigns a {@link PathProvider}.
     *
     * @param propertyName name of the attribute that this PropertyMatcher matches against in the target object
     * @param registry the PropertyMatcherRegistry to register with; can be null
     * @param pathProvider provides this PropertyMatcher with its path context. I.e. the property path that leads
     * to the object containing this attribute in the target object graph; can be null
     */
    public PropertyMatcher(String propertyName, PropertyMatcherRegistry registry, PathProvider pathProvider) {
        if (propertyName == null) {
            throw new IllegalArgumentException("No property name");
        }

        this.propertyName = propertyName;

        if (pathProvider != null) {
            setPathProvider(pathProvider);
        }

        if (registry != null) {
            registry.registerPropertyMatcher(this);
        }
    }

    /**
     * Gets the assigned path provider.
     *
     * @return the assigned PathProvider, or null if no PathProvider has been assigned
     */
    public PathProvider getPathProvider() {
        return pathProvider;
    }

    /**
     * Sets the path provider, which provides this PropertyMatcher with its path context. I.e. the property path that leads
     * to the object containing this attribute in the target object graph.
     *
     * @param pathProvider the path provider
     */
    public void setPathProvider(PathProvider pathProvider) {
        this.pathProvider = pathProvider;
    }

    /**
     * Sets the matcher that the property that this instance represents in the target object graph must match.
     * <p>
     * If matcher is null, this PropertyMatcher will be ignored.
     * <p>
     * If the matcher is {@link PathAware}, this instance will become its {@link PathProvider}.
     *
     * @param matcher the matcher
     */
    public void setMatcher(Matcher<? super T> matcher) {
        this.matcher = matcher;
        if (matcher instanceof PathAware) {
            ((PathAware) matcher).setPathProvider(this);
        }
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getPath() {
        if (pathProvider == null) {
            throw new IllegalStateException("No PathProvider assigned");
        }

        String pathContext = pathProvider.getPath();
        return pathContext + (pathContext.length() > 0 ? "." : "") + propertyName;
    }

    public boolean matches(Object item) {
        return matcher == null || matcher.matches(item);
    }

    public boolean isSpecified() {
        return matcher != null;
    }

    public void describeTo(Description description) {
        description.appendText("has ").appendText(propertyName).appendText(" (");

        if (matcher != null) {
            description.appendDescriptionOf(matcher);
        } else {
            description.appendText("<any>");
        }

        description.appendText(")");
    }

    public void describeMismatch(Object item, Description mismatchDescription) {
        if (matcher instanceof PathAware) {
            // PathAware matchers can take care of their own describeMismatch
            matcher.describeMismatch(item, mismatchDescription);
        } else {
            // Non-PathAware matchers need their mismatch description augmenting with
            // the path of the property being matched and the expected value...
            mismatchDescription
                    .appendText(getPath())
                    .appendText(" ");

            matcher.describeMismatch(item, mismatchDescription);

            mismatchDescription
                    .appendText(" (expected ")
                    .appendDescriptionOf(matcher)
                    .appendText(")");
        }

        // Note: the PathAware/non-PathAware distinction may be artificial.
        // Really we are interested in whether the matcher can fully describe itself in terms
        // of its path within a matched object graph. It may be better to check if the matcher extends
        // PathAwareDiagnosingMatcher.
    }
}
