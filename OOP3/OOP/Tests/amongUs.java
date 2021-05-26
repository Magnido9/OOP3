package OOP.Tests;


import OOP.Provided.*;
import OOP.Solution.Inject;
import OOP.Solution.Injector;
import org.junit.Test;

import OOP.Solution.Provides;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class amongUs {

    static class gamer {
    }

    static class Crewmate extends gamer {
        @Inject
        public Crewmate() {
        }    // This constructor should be called because it is injected


        public void situation() {
            System.out.println("im not sus");
        }


        public Crewmate(Object o) {
        }    // This one exists but isn't injected
    }

    @Test
    public void test_provided() {

        Injector i = new Injector();
        try {
            i.bind(gamer.class, Crewmate.class);
            Object o = i.construct(gamer.class);
            assertEquals(o.getClass(), Crewmate.class);
            // System.out.println(o);      // Should be instance of Subclazz

        } catch (Exception e) {
            System.out.println(("player is not a crewmate , how??"));
            e.printStackTrace();
        }
        Injector susinjection = new Injector(){

            @Provides
            @Message
            public String reveal(){
                int x = game.x%2;
                if(x ==1){
                    return "sus";
                }
                return "im not sus";
//                int x = ((int)(Math.random()*10))%2;
//                if(x ==1){
//                    return "sus";
//                }
//                return "im not sus";
            }
            @Provides
            @Message
            public Integer add(){
                game.x +=1;
                return game.x;
            }
        };

        try {
            susinjection.bind(Player.class,pla.class);
        } catch (IllegalBindException e) {
            e.printStackTrace();
        }
        Injector i2 = new Injector(){
            @Provides
            @Message
            // Now if someone will request a @Message they will receive this message
            public float test() {
                game amongsus =new game();
                for( Integer i =0 ; i < 10 ;i++){
                    try {
                        amongsus.addplayer((pla)susinjection.construct(Player.class));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                assert(amongsus.playerlist.size()==10);
                amongsus.getVotes();


                return (float)(0.5);
            }




        };

        try {
            game mp = (game) i2.construct(game.class);
            //mp.play((float)(0.2));
        } catch (Exception e) {
            System.out.println(("we goofed"));
            e.printStackTrace();
        }
        //new MessagePrinter("@34");
    }


    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER, ElementType.METHOD})
    @interface Message {
    }

    public enum Colors {RED , YELLOW , GREEN ,BLACK , WHITE, BLUE, CYAN, PURPLE, VELVET, BROWN, ORANGE  };
    static class Player {

    }
    static class pla extends  Player {
        String color;
        String message = "im not sus";

        @Inject
        public pla(@Message Integer x,@Message String wut) {
            color = (Colors.values()[x]).name();
            message=wut;
        }

    }

    static class game {
        List<pla> playerlist = new LinkedList<>();
        static int x=0;
        // This constructor is chosen because it is injected
        // In addition, there's a @Message parameter therefore the injector
        // searches for a suitable @Provider.
        // It will find the provideMessage() method on lines 36-41 and call it
        // Later, when print() is called, MessagePrinter will print "Hello, world!"
        @Inject
        public game(@Message float message) {
            ;
        }

        public game(){}
        void addplayer(pla player){
            playerlist.add(player);
        }
        void getVotes(){
            int suses = 0;
            int crewmates=0;
            for(pla p : playerlist){
                if(p.message=="im not sus"){
                    crewmates +=1;
                }
                else {
                    suses +=1;
                }

            }
            assert(suses==crewmates);
//            System.out.println("total of sus = "+suses+"\n total of crewmates="+crewmates);


        }
    }
}