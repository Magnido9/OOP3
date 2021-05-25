
package OOP.Tests;

import OOP.Provided.IllegalBindException;
import OOP.Solution.Injector;
import OOP.Solution.Inject;

import OOP.Solution.Named;
import OOP.Solution.Provides;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExampleTest {

    static class Superclazz {
    }

    static class Subclazz extends Superclazz {
        @Inject
        public Subclazz() {
        }    // This constructor should be called because it is injected

        @Inject
        public void shuga() {
           // System.out.println("damn honey");
        }

        @Inject
        private void dontprintme() {
            //System.out.println("why was i printed?");
        }


        public Subclazz(Object o) {
        }    // This one exists but isn't injected
    }

    static class myClass {
    }

    static class mySub extends myClass {
        @Inject
        public mySub() {
        }    // This constructor should be called because it is injected


        @Inject
        public void thisisgood(Superclazz A) {
          //  System.out.println("x is:");
          //  System.out.println("did i print?");
        }

        @Inject
        private void dontprintmeeveragain() {
       //     System.out.println("stop addressing private methods");
        }


        public mySub(Object o) {
        }    // This one exists but isn't injected
    }

    @Test
    public void test_anonymous() {

        Injector i = new Injector();
        try {
            i.bind(Superclazz.class, Subclazz.class);
            Object o = i.construct(Superclazz.class);
            assertEquals(o.getClass(), Subclazz.class);
            i.bind(myClass.class, mySub.class);
            o = i.construct(myClass.class);

            // System.out.println(o);      // Should be instance of Subclazz

        } catch (Exception e) {
            System.out.println(("we goofed1"));
            e.printStackTrace();
        }

        Injector i2 = new Injector() {
            @Provides
            @Message
            // Now if someone will request a @Message they will receive this message
            public String provideMessage() {
              //  System.out.println("x is :");
                return "Hello, world!";
            }
        };

        try {
            MessagePrinter mp = (MessagePrinter) i2.construct(MessagePrinter.class);
            mp.print();
        } catch (Exception e) {
            System.out.println(("we goofed2"));
            e.printStackTrace();
        }
        new MessagePrinter("@34");
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER, ElementType.METHOD})
    @interface Message {
    }

    static class MessagePrinter {
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


    @Test
    public void test_bind_0() {
        Injector i = new Injector();
        assertDoesNotThrow(() -> {
            i.bind(Superclazz.class, Subclazz.class);
            Object o = i.construct(Superclazz.class);
            assertEquals(o.getClass(), Subclazz.class);
        });

        assertDoesNotThrow(() -> {
            i.bindToInstance(Class.class, Subclazz.class);
            Object o = i.construct(Class.class);
            assertEquals(o, Subclazz.class);
        });

        assertThrows(IllegalBindException.class, () -> i.bind(Subclazz.class, Superclazz.class));

        //assertThrows(IllegalBindException.class, () -> i.bind(Superclazz.class, Superclazz.class));
        //assertThrows(IllegalBindException.class, () -> i.bind(Subclazz.class, Subclazz.class));
    }

    static class Identified {
        static int countInstances = 0;
        int id;

        public Identified() {
            this.id = countInstances++;
        }

        static void reset() {
            countInstances = 0;
        }

        Integer getId() { return id;}

        @Override
        public boolean equals(Object other) {
            if (!Identified.class.isAssignableFrom(other.getClass())) {
                return false;
            }

            return this.id == ((Identified) other).id;
        }
    }

    static public class SubIdentified extends Identified {
        public SubIdentified() {}
    }

    @Test
    public void test_instance_0() {
        Injector i = new Injector();
        assertThrows(IllegalBindException.class, () -> i.bindToInstance(Identified.class, 0));
        assertThrows(IllegalBindException.class, () -> i.bindToInstance(Identified.class, null));
        assertThrows(IllegalBindException.class, () -> i.bindToInstance(Identified.class, "hi"));
        assertThrows(IllegalBindException.class, () -> i.bindToInstance(Identified.class, new Subclazz()));
        //assertThrows(IllegalBindException.class, () -> i.bindToInstance(Identified.class, Identified.class));
        assertThrows(IllegalBindException.class, () -> i.bindToInstance(Identified.class, Class.class));
        assertThrows(IllegalBindException.class, () -> i.bindToInstance(Identified.class, Object.class));
    }

    @Test
    public void test_instance_1() {
        assertDoesNotThrow(() -> {
            Injector i = new Injector();
            Identified inst = new Identified();
            i.bindToInstance(Identified.class, inst);
            for (int j = 0; j < 10; j++)
                assertEquals(inst, i.construct(Identified.class));

            Identified another = new Identified();
            assertNotEquals(another, inst);
            assertNotEquals(another, i.construct(Identified.class));

            i.bindToInstance(Identified.class, another);
            for (int j = 0; j < 10; j++) {
                assertEquals(another, i.construct(Identified.class));
                assertNotEquals(inst, i.construct(Identified.class));
            }
        });
    }

    @Test
    public void test_instance_2(){
        Injector i = new Injector(){
            public String inst = "A";

            @Override public String toString() {
                inst = inst + inst;
                return inst;
            }
        };

        assertDoesNotThrow(()-> {
            i.bindToInstance(String.class, i.toString());
            assertEquals("AA", i.construct(String.class));
            assertEquals("AA", i.construct(String.class));
            i.bindToSupplier(String.class, ()->i.toString());
            assertEquals("AAAA", i.construct(String.class));
            assertEquals("AAAAAAAA", i.construct(String.class));
            assertEquals("AAAAAAAAAAAAAAAA", i.construct(String.class));
        });
    }


    @Test
    public void test_instance_3() {
        Injector i = new Injector();
        assertDoesNotThrow(()->{
            i.bindToInstance(Identified.class, new Identified(){
                @Override Integer getId() { return 999;}
            });
            Identified iden = (Identified) i.construct(Identified.class);
            assertEquals(new Integer(999), iden.getId());
        });



    }

    void test_supplier(Injector i) throws IllegalBindException {
        int J = 10;

        Supplier<Object> sup = Identified::new;


                i.bindToSupplier(Identified.class, sup);

        for (int k = 0; k < 3; k++) {
            Identified.reset();
            for (int j = 0; j < J; j++) {
                int finalJ = j;
                assertDoesNotThrow(() -> {
                    Identified output = (Identified) i.construct(Identified.class);
                    assertEquals(finalJ, output.id);
                });
            }
            assertEquals(J, Identified.countInstances);
        }
    }

    void test_instance(Injector i) {
        Identified.reset();
        Identified inst = new Identified();
        assertDoesNotThrow(() -> {
            i.bindToInstance(Identified.class, inst);
        });
        assertEquals(1, Identified.countInstances);
        assertDoesNotThrow(() -> {
            for (int j = 0; j < 10; j++) {
                assertEquals(inst, i.construct(Identified.class));
            }
            assertEquals(1, Identified.countInstances);
        });
    }

    void test_ctor(Injector i){
        int K = 3, J = 10;
        assertDoesNotThrow(()->{
            i.bind(Identified.class, SubIdentified.class);
            for (int k = 0; k < K; k++) {
                Identified.reset();
                for (int j = 0; j < J; j++) {
                    int finalJ = j;
                    assertDoesNotThrow(()->{
                        Identified output = (Identified) i.construct(Identified.class);
                        assertEquals(finalJ, output.id);
                    });
                }
                assertEquals(J, Identified.countInstances);
            }
        });

    }

    @Test
    public void test_supplier_1() throws IllegalBindException {
        Injector i = new Injector();
        test_supplier(i);
        test_instance(i);
        test_ctor(i);
        test_instance(i);
        test_supplier(i);
        test_ctor(i);
        test_supplier(i);
    }


    @Test
    public void test_provided() {

        Injector i = new Injector();
        try {
            i.bind(Superclazz.class, Subclazz.class);
            Object o = i.construct(Superclazz.class);
            assertEquals(o.getClass(), Subclazz.class);
            // System.out.println(o);      // Should be instance of Subclazz

        } catch (Exception e) {
            System.out.println(("we goofed"));
            e.printStackTrace();
        }

        Injector i2 = new Injector() {
            @Provides
            @Message
            // Now if someone will request a @Message they will receive this message
            public String provideMessage() {
                return "Hello, world!";
            }
        };

        try {
            MessagePrinter mp = (MessagePrinter) i2.construct(MessagePrinter.class);
            mp.print();
        } catch (Exception e) {
            System.out.println(("we goofed"));
            e.printStackTrace();
        }
        new MessagePrinter("@34");
    }


    /////////////// Provides
    //@Test
//    public void test_provides_0(){
//        Injector i = new Injector(){
//            @Provides
//            Integer IntegerProvider(){
//                return 0;
//            }
//        };
//
//        assertDoesNotThrow(()->
//                assertEquals(0, i.construct(Integer.class))
//        );
//    }

    static class AnyInteger {
        int x;
        boolean wasInjected = false;
        public AnyInteger() {
            this.x = 9999;
        }

        @Inject
        AnyInteger(@Named("Zero") AnyInteger other) {
            this.x = other.x;
        }
        @Inject void somethingToInject() {wasInjected = true;}
    }

    static class JustZero extends AnyInteger {
        JustZero() {
            this.x = 0;
            // System.out.println("i got here");}
        }
    }


    @Test
    public void test_named_0() throws IllegalBindException {
        Injector i = new Injector();
        i.bindByName("Zero", JustZero.class);

        assertDoesNotThrow(() -> {
            i.construct(AnyInteger.class);
        });
//        System.out.println("s");
    }

//////////////////////
    // test for field
//////////////////////
    private static class Superfields {
        @Inject AnyInteger x;
        @Inject Superclazz supy;
        boolean wasInjected = false;

        public Superfields() {
            x = new AnyInteger();
            this.x.x = 9999;
        }

        @Inject
        Superfields(@Named("Juan") AnyInteger other) {
            this.x = other;
        }

        public boolean isCorrect(){
            if(x.x==0 && supy.getClass()==Subclazz.class && x.wasInjected && wasInjected){
                return true;
            }
            return false;
        }

        @Inject
        void somethingToInject() {wasInjected = true;}
    }

    static class Juan extends AnyInteger {
        Juan() {
            this.x = 1;
            // System.out.println("i got here");}
        }
    }

/*
 this tests that you called instantiation of injected fields
 */

    @Test
    public void fields_test_0() {
        try {
            Injector i = new Injector();
            i.bind(Superclazz.class, Subclazz.class);
            i.bindByName("Zero", JustZero.class);
            i.bindByName("Juan", Juan.class);
            assertDoesNotThrow(() -> {
                i.construct(Superfields.class);
            });
            assertDoesNotThrow(() -> {
                Superfields o = (Superfields) i.construct(Superfields.class);
                assert (o.isCorrect());
            });
        }
        catch (Exception e){
            System.out.println("mistake in fields test");
        }
//        System.out.println("s");
    }



    /*
     this tests that you called instantiation of injected fields after calling to injected methods
     */
    static class tmpclass{
        int x;
        tmpclass(){
            x=10;
        }
    }
    static class FieldsOverMethods  {
        @Inject tmpclass x;

        @Inject
        FieldsOverMethods() {
            x= new tmpclass();
            x.x=1;
            // System.out.println("i got here");}
        }

        @Inject
        void ChangeX() {
            x.x=420;
        }
    }

    @Test
    public void fields_test_1() {
        try {
            Injector i = new Injector();
            assertDoesNotThrow(() -> {
                Object o =i.construct(FieldsOverMethods.class);
                assert(((FieldsOverMethods)(o)).x.x == 10);
            });
        }
        catch (Exception e){
            System.out.println("mistake in order (needed order = first methods than fields)");
        }
//        System.out.println("s");
    }
    static public class SubSuperfields extends Superfields {
        public SubSuperfields() {}
    }

    static private class PrivateSubSuperfields extends Superfields {
        private PrivateSubSuperfields() {}
    }

/*
 this tests access to private constructors
 */
    @Test
    public void fields_test_2() {
        try {
            Injector i = new Injector();
            i.bind(Superclazz.class, Subclazz.class);
            i.bindByName("Zero", JustZero.class);
            i.bindByName("Juan", Juan.class);
            assertDoesNotThrow(() -> {
                i.construct(PrivateSubSuperfields.class);
            });
            assertDoesNotThrow(() -> {
                Superfields o = (Superfields) i.construct(PrivateSubSuperfields.class);
            });

            i.bind(Superfields.class, PrivateSubSuperfields.class);
            assertDoesNotThrow(() -> {
                i.construct(Superfields.class);
            });

        }
        catch (Exception e){
            System.out.println("didnt manage to use private constructor");
        }
//        System.out.println("s");
    }



//////////////////////
    // end test for field
//////////////////////

    //////////////////////// Race
    // Donkey < HouseCat < Horse < Cheetah
    static class Animal {
        int speed;

        public int getSpeed() {
            return speed;
        }
    }

    static class Equus extends Animal {
    }

    static class Horse extends Equus {
        public Horse() {
            speed = 88;
        }
    }

    static class Donkey extends Equus {
        public Donkey() {
            speed = 24;
        }
    }

    static class CrippleHorse extends Horse {
        public CrippleHorse() {
        }

        @Inject
        public void makeCripple() {
            speed = 0;
        }
    }

    static class Cat extends Animal {
    }

    static class Cheetah extends Cat {
        public Cheetah() {
            speed = 130;
        }
    }

    static class HouseCat extends Cat {
        public HouseCat() {
            speed = 48;
        }
    }

    public static class Race {
        @Inject
        public Equus equusContestant;
        @Inject
        public Cat catContestant;

        public Race() {
        }

        ;
//        Race(Equus equusContestant, Cat catContestant) {
//            this.equusContestant = equusContestant;
//            this.catContestant = catContestant;
//        }


        Animal getWinner() {
            if (equusContestant.getSpeed() < catContestant.getSpeed())
                return catContestant;
            else
                return equusContestant;
        }

        boolean didTheCatWin() {
            return getWinner() == catContestant;
        }
    }

    static class RaceTest {
        Class equus, cat;
        boolean doesTheCatWin;

        RaceTest(Class equus, Class cat, boolean doesTheCatWin) {
            this.equus = equus;
            this.cat = cat;
            this.doesTheCatWin = doesTheCatWin;
        }
    }

    @Test
    public void test_race_0() {

        Injector i = new Injector();

        RaceTest raceTests[] = {
                // Donkey < HouseCat < Horse < Cheetah
                new RaceTest(Donkey.class, HouseCat.class, true),
                new RaceTest(Donkey.class, Cheetah.class, true),
                new RaceTest(Horse.class, Cheetah.class, true),
                new RaceTest(Horse.class, HouseCat.class, false),

                // CrippleHorse always loses
                new RaceTest(CrippleHorse.class, Cheetah.class, true),
                new RaceTest(CrippleHorse.class, HouseCat.class, true),
        };

        for (RaceTest raceTest : raceTests) {
            assertDoesNotThrow(() -> {
                i.bind(Equus.class, raceTest.equus);
                i.bind(Cat.class, raceTest.cat);

                Race race = (Race) i.construct(Race.class);
                assertEquals(raceTest.doesTheCatWin, race.didTheCatWin());
            });
        }
    }
}
