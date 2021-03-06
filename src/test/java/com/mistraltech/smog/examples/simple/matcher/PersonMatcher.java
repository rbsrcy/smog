package com.mistraltech.smog.examples.simple.matcher;

import com.mistraltech.smog.core.MatchAccumulator;
import com.mistraltech.smog.core.PropertyMatcher;
import com.mistraltech.smog.core.ReflectingPropertyMatcher;
import com.mistraltech.smog.examples.model.Person;
import com.mistraltech.smog.examples.model.Phone;
import org.hamcrest.Matcher;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;

public class PersonMatcher extends AddresseeMatcher<PersonMatcher, Person> {
    private PropertyMatcher<Integer> ageMatcher = new PropertyMatcher<Integer>("age", this);
    private PropertyMatcher<List<Phone>> phoneListMatcher = new ReflectingPropertyMatcher<List<Phone>>("phoneList", this);

    private PersonMatcher() {
        super("a Person");
    }

    public static PersonMatcher aPersonThat() {
        return new PersonMatcher();
    }

    public PersonMatcher hasAge(int age) {
        return this.hasAge(equalTo(age));
    }

    public PersonMatcher hasAge(Matcher<? super Integer> ageMatcher) {
        this.ageMatcher.setMatcher(ageMatcher);
        return this;
    }

    public PersonMatcher hasPhoneList(Matcher<? super List<? extends Phone>> phoneListMatcher) {
        this.phoneListMatcher.setMatcher(phoneListMatcher);
        return this;
    }

    protected void matchesSafely(Person item, MatchAccumulator matchAccumulator) {
        super.matchesSafely(item, matchAccumulator);
        matchAccumulator.matches(ageMatcher, item.getAge());
    }
}
