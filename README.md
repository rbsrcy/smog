# SMOG

Sparse Matching of Object Graphs - an extension to Hamcrest

## Summary

Problem:

You are writing JUnit-style tests for operations that update an object graph in various ways. In each test, you want to assert that specific properties of the objects in the object graph are correct.

Solution:

Write a matcher for each class in the object graph based on the SMOG library, then compose instances of those matcher classes into a matcher object tree for each test. Each matcher tree matches just the properties you care about for that test.

Add a single Hamcrest assertion to each of your tests asserting that the actual resulting object graph matches your expected object graph.

If any of the properties do not match, the assertion will fail with a message that allows you to easily identify which property or properties within the object graph didn't match and what their expected and actual values were.

## SMOG Matcher Usage Examples
Here are a few examples to illustrate how SMOG matchers can be used. They're just examples. They're not real.
### Simplest Case
For a simple case, let's just match some property values on a single object.
Assume you are testing an Account class:
```
public class Account() {
    private String owner;
    private int balance;
    
    public Account(String owner, int balance) {
        this.owner = owner;
        this.balance = balance;
    }
    
    public String getOwner() { return owner; }
    public int getBalance() { return balance; }
    public boolean isOverdrawn() { return balance < 0; }
    ...
}
```
Using SMOG matchers, we can test Account like this:
```
@Test
public void canConstructAccount()
{
    Account account = new Account("bob", 100);
  
    assertThat(account, is(anAccountThat().hasOwner("bob").hasBalance(100).hasOverdrawn(false));
}

@Test
public void canGoOverdrawn()
{
    Account account = new Account("bob", 100);
  
    account.withdraw(150);
  
    assertThat(account, is(anAccountThat().hasBalance(-50).hasOverdrawn(true));
}
```
Suppose instead of subtracting the withdrawn amount, we added it. The test would fail and the output would look like this:
```
java.lang.AssertionError
Expected: is an Account that (has balance (-50) and overdrawn (true))
     but: balance was 250 (expected -50)
     and: overdrawn was false (expected true)
```
### Matching Nested Objects
A slightly more complex example might involve a transfer of funds using a Transfer object:
```
public class Transfer() {
    private Account from;
    private Account to;
    private int amount;
    
    public Transfer(Account from, Account to, int amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
        from.withdraw(amount);
        to.deposit(amount);
    }
    
    public Account getFromAccount() { return from; }
    public Account getToAccount() { return to; }
    public int getAmount() { return amount; }
}
```
Now, when the transfer occurs, we want the associated accounts to have been updated:
```
@Test
public void fundsAreTransferred()
{
  Account fredsAccount = new Account("fred", 100);
  Account tracysAccount = new Account("tracy", 100);
  
  Transfer transfer = new Transfer(fredsAccount, tracysAccount, 50);
  
  assertThat(transfer, is(aTransferThat()
      .hasAmount(50)
      .hasFromAccount(anAccountThat()
          .hasOwner("fred")
          .hasBalance(50))
      .hasToAccount(anAccountThat()
          .hasOwner("tracy")
          .hasBalance(150))
      ));
}
```
Suppose instead of depositing the money in Tracy's account, we withdrew it. The test would fail and the output would look like this:
```
java.lang.AssertionError
Expected: is a Transfer that (has amount (50) and fromAccount (an Account that (has owner ("fred") and balance (50))) and toAccount (an Account that (has owner ("tracy") and balance (150))))
     but: toAccount.balance was 50 (expected 150)
```
Or we may have stored the first constructor argument as both the 'from' and 'to' accounts. We would then see:
```
java.lang.AssertionError
Expected: is a Transfer that (has amount (50) and fromAccount (an Account that (has owner ("fred") and balance (50))) and toAccount (an Account that (has owner ("tracy") and balance (150))))
     but: toAccount.owner was "fred" (expected "tracy")
     and: toAccount.balance was 50 (expected 150)
```
##FAQs
### Why do I need SMOG?

You can easily write matcher classes that match the properties of an object without using SMOG. But what happens when the match fails? You may get a message containing a java object reference for the object that didn't match. If you're lucky and toString has been overridden you may instead get a list of all the properties of an object. You are then left to work out which property caused the failure. This might not be too bad for an object with a couple of properties, but as the number of properties grows it can become a real headache.

SMOG tracks the path to each property so that, when a match fails, it can give you a message that describes exactly which properties didn't match and why. If multiple properties didn't match, it lists all of them. And it works within the Hamcrest framework, so if the original assertion reads well, the failure message should read well too.

### Couldn't I just use assertEquals in my tests?

Assert.assertEquals is fine for primitive values. If you're using it to compare objects though, there are some issues. Firstly you must _fully_ construct your expectation object. This means setting values even if you don't care about them in the test. This adds noise to the test, obscuring the intent, and makes the test fragile. Also, equals() may not be overridden on the object you are testing, or may not do what your test wants it to. Persistent entities often define equals() to mean something other than "_do all the properties on these two objects match?_". Even if equals() does mean that now, will it always mean that? If you change the definition of equals(), you've probably broken all your tests (and not necessarily in a way that is obvious).

You could of course write test helper methods with a test-specific implementation of equals(), but you will still have the problem of noisy and fragile tests.

### Couldn't I just write multiple assertions?

You could write a separate assertion for each property you care about. But then, only the first failure will be reported. Your code will also be more verbose.

### Is this style of testing a good idea?

Not necessarily. Certainly it is possible to write complex test expectations that are hard to read and maintain. And it may lead to tests that do too much - what Gerard Meszaros calls an Eager Test (xUnit Test Patterns). Basically, it is up to you to use it wisely :)

### I'm using matchers in mocks - how does SMOG help?

Mocking libraries like Mockito support using Hamcrest matchers to match arguments in mock expectations. SMOG matchers are Hamcrest matchers, so they can be used too. The issue becomes how you diagnose a mismatch, since there is no helpful output message from the matcher. SMOG provides a LoggingCompositePropertyMatcher class that can be used as the base class for composite matchers instead of CompositePropertyMatcher. This will log all mismatches as DEBUG messages to a configured SLF4J logging library.

## Acknowledgments

This project builds on the work and ideas of developers on the Titan Pricing Management (v1) application at
Black Pepper Software (http://blackpepper.co.uk/).

The project extends the Hamcrest Matcher library at http://hamcrest.org/ and relies heavily on the mechanism introduced in the TypeSafeDiagnosingMatcher.


