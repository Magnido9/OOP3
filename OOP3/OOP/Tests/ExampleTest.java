package OOP.Tests;

import OOP.Solution.Injector;
import OOP.Solution.Inject;
import OOP.Solution.Provides;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class ExampleTest {

    public static class Superclazz {}

    public static class Subclazz extends Superclazz {
        @Inject
        public Subclazz() {}    // This constructor should be called because it is injected

        public Subclazz(Object o) {}    // This one exists but isn't injected
    }

    public static void main(String[] args) {

        Injector i = new Injector();
        try {
            i.bind(Superclazz.class, Subclazz.class);
            Object o = i.construct(Superclazz.class);
            System.out.println(o);      // Should be instance of Subclazz
        } catch (Exception e) {
            e.printStackTrace();
        }

        Injector i2 = new Injector(){
            @Provides
            @Message
            // Now if someone will request a @Message they will receive this message
            public String provideMessage() {
                return "Hello, world!";
            }
        };

        try {
            MessagePrinter mp = (MessagePrinter)i2.construct(MessagePrinter.class);
            mp.print();
        } catch (Exception e) {
            e.printStackTrace();
        }
        new MessagePrinter("@34");
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER, ElementType.METHOD})
    @interface Message {}

    public static class MessagePrinter {
        private final String message;
        // This constructor is chosen because it is injected
        // In addition, there's a @Message parameter therefore the injector
        // searches for a suitable @Provider.
        // It will find the provideMessage() method on lines 36-41 and call it
        // Later, when print() is called, MessagePrinter will print "Hello, world!"
        @Inject
        public MessagePrinter(@Message String message) {
            this.message = message;
        }
        public void print() {
            System.out.println(this.message);
        }
    }
}
