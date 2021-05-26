package OOP.Tests;

import org.junit.Test;
import OOP.Solution.*;
import OOP.Provided.*;

import javax.management.DescriptorKey;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;


import static org.junit.Assert.assertEquals;

public class ExtraTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER, ElementType.METHOD})
    @interface Address{}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER, ElementType.METHOD})
    @interface Message{}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER, ElementType.METHOD})
    @interface Envelope{}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER, ElementType.METHOD})
    @interface Stamp{}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER, ElementType.METHOD})
    @interface Code{}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER, ElementType.METHOD})
    @interface Colour{}



    class envelope{


        String size;
        String colour;
        @Inject
        public envelope(@Named( "normal") String size, @Colour String colour) {
            this.size = size;
            this.colour = colour;
        }

        @Override
        public String toString() {
            return
                    "size: " + size + '\'' +
                    " colour: " + colour;
        }
    }
    class Person {
        String name;

        public Person(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    class Reciever extends Person{

        public Reciever(String name) {
            super(name);
        }

        @Override
        public String toString() {
            return name;
        }
    }


    public class SuperInjector extends Injector{

        @Provides
        @Address
        private int getAddress(){
            return 123456789;
        }

        @Provides
        @Stamp
        private String getStamp(){
            return "Very fancy Stamp";

        }

        @Provides
        @Code
        String stringCode(){
            return "Secret Code";

        }




    }

    public class DerivedInjector extends SuperInjector{

        @Provides
        @Envelope
        private envelope getEnvelope(){

            try {
                return (envelope) this.construct(envelope.class);
            }catch (Exception e){
                System.out.println("something bad happened" + e.toString());
                e.printStackTrace();

            }
            return null;
        }

        @Provides
        @Message
        private String getMessage(){
            return "Deep and profound message";

        }

        @Provides
        @Colour
        private String getColour(){
            return "White";

        }


        @Provides
        @Address
        private String getAddress(){
            return "Ulmann 703";

        }



    }


    public class Letter{
        envelope pack;
        String stamp;
        String address;
        Person reciever;
        String message;
        @Inject
        public Letter(@Envelope envelope pack,@Stamp String stamp,@Address String address,@Named("To whom it may concern") Person reciever,@Message String message) {
            this.pack = pack;
            this.stamp = stamp;
            this.address = address;
            this.reciever = reciever;
            this.message = message;
        }

        @Override
        public String toString() {
            return "Letter: " +
                    " pack: " + pack +'\n'+
                    " stamp: " + stamp + '\n' +
                    " address: " + address + '\n' +
                    " reciever: " + reciever +'\n' +
                    " message: " + message;
        }
    }
    @Test
    public void AdvancedTest_1(){ //Tests a mix of named, provides and Inject
        DerivedInjector inj = new DerivedInjector();

        Reciever recv = new Reciever("Yosef");

        try {
            inj.bindByName("normal",String.class);
            inj.bindToInstance(String.class,"A4");
            inj.bindByName("To whom it may concern", Reciever.class);
            inj.bind(Person.class,Reciever.class);
            inj.bindToInstance(Reciever.class,recv);

       Letter l  =  (Letter) inj.construct(Letter.class);
            assertEquals(l.toString(),
                    "Letter:  pack: size: A4' colour: White\n" +
                    " stamp: Very fancy Stamp\n" +
                    " address: Ulmann 703\n" +
                    " reciever: Yosef\n" +
                    " message: Deep and profound message");

        }catch(Exception e) {}

    }

    public static class Car{

        private String colour;
        private Integer max_speed;
        private String model;


        private Car(){
            this.colour = "Red";
            this.max_speed = 50;
            this.model = "honda ";
        }



        @Inject
        private Person driver;



        @Inject
        private void UpgradeSpeed(){
            this.max_speed +=100;
        }
        @Inject
        private void PaintJob(){
            this.colour = "Yellow";
        }

        @Inject
        private void ImproveModel(){
           this.model = "Lamborghini";
        }


        @Override
        public String toString() {
            return "Car: " + '\n' +
                    "colour: " + colour + '\n' +
                    "max_speed: " + max_speed + '\n' +
                    "model: " + model + '\n' +
                    "driver: " + driver ;
        }
    }




    @Test
    public void default_injector(){ //Tests inject on default ctor, kinda basic
        try{




            Person yosef =new Person("Yosef");
            Injector inj = new DerivedInjector();
            inj.bindToInstance(Person.class,yosef);
           Car my_car = (Car) inj.construct(Car.class);


        assertEquals(my_car.toString(),
                "Car: \n" +
                "colour: Yellow\n" +
                "max_speed: 150\n" +
                "model: Lamborghini\n" +
                "driver: Yosef");



        }catch(Exception e){}


    }
    public class CodeFetcher{
        String code;

        public CodeFetcher() {
           this.code = "hello";
        }

        @Override
        public String toString() {
            return code;
        }
    }


    public class Decoder {
      private  Integer Key;
      private String code;
        @Inject
        public Decoder(@Named("master") Integer key) {
            Key = key;
            code = "";
        }
        @Inject
        public void dechyper() throws MultipleInjectConstructorsException, NoSuitableProviderFoundException, NoConstructorFoundException, MultipleProvidersException, MultipleAnnotationOnParameterException, InvocationTargetException, InstantiationException, IllegalAccessException {
            Injector inj = new DerivedInjector();
            CodeFetcher cf = (CodeFetcher) inj.construct(CodeFetcher.class); //this should fail
        }
    }


    public class A {
        public Integer Key;

        @Inject
        public A() {
            Key = 1;
        }
    }

        public class B extends A {
            public Integer Key;

            @Inject
            public B() {
                Key = 2;
            }
        }
    @Test
    public void TestBindingtoSelf(){
        Injector inj = new DerivedInjector();
        try {

            CodeFetcher c = new CodeFetcher();

            inj.bind(CodeFetcher.class, CodeFetcher.class);
            CodeFetcher cf = (CodeFetcher) inj.construct(CodeFetcher.class);
            assertEquals("hello",cf.toString());


            inj.bind(A.class,B.class);
            inj.bind(A.class,A.class);
            A a = (A) inj.construct(A.class);
            Integer i = 1;
            assertEquals(a.Key,i);




            inj.bind(c.getClass(),CodeFetcher.class);
            CodeFetcher cf1 = (CodeFetcher) inj.construct(CodeFetcher.class);
            assertEquals("hello",cf1.toString());
        }catch(Exception e){}
    }

































}
