package OOP.Solution;

import OOP.Provided.IllegalBindException;
import OOP.Provided.MultipleAnnotationOnParameterException;
import OOP.Provided.MultipleInjectConstructorsException;
import OOP.Provided.NoConstructorFoundException;

import javax.management.ObjectInstance;
import java.lang.annotation.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Supplier;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD,ElementType.CONSTRUCTOR})
 @interface Named {}
public class Injector {
    HashMap<Class,Class> classBinds;
    HashMap<String,Class> nameBinds;
    HashMap<Class,Object> instanceBinds;
    HashMap<Class,Supplier> supplierBinds;
    public void bind(Class clazz1, Class clazz2) throws IllegalBindException{
        if(clazz1.isAssignableFrom(clazz2))
            classBinds.put(clazz1,clazz2);
        else
            throw new IllegalBindException();
    }
    public void bindToInstance(Class clazz1, Object clazz2) throws IllegalBindException{
        if(clazz1.isAssignableFrom(clazz2.getClass()))
            instanceBinds.put(clazz1,clazz2);
        else
            throw new IllegalBindException();
    }
    public void bindToName(Class clazz1, String clazz2) throws IllegalBindException{
        if(clazz2.getClass().equals(clazz1))
            nameBinds.put(clazz2,clazz1);
        else
            throw new IllegalBindException();
    }
    public void bindToSupplier(Class clazz1, Supplier clazz2) throws IllegalBindException{
        if(clazz2.getClass().equals(clazz1))
            supplierBinds.put(clazz1,clazz2);
        else
            throw new IllegalBindException();
    }
    public Object construct(Class clazz) throws MultipleInjectConstructorsException, NoConstructorFoundException, MultipleAnnotationOnParameterException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if(clazz.getConstructors().length>1)
            throw new MultipleInjectConstructorsException();
        else if(clazz.getConstructors().length<1)
            throw new NoConstructorFoundException();
        else if(classBinds.containsKey(clazz))
            return construct(classBinds.get(clazz));
        else if(instanceBinds.containsKey(clazz))
            return instanceBinds.get(clazz);
        else if(supplierBinds.containsKey(clazz))
            return supplierBinds.get(clazz).get();
        else if(Arrays.stream(clazz.getConstructors()).filter(a->a.isAnnotationPresent(Inject.class)).toArray().length>1)
            throw new MultipleInjectConstructorsException();
        else if(Arrays.stream(clazz.getConstructors()).filter(a->a.isAnnotationPresent(Inject.class)).toArray().length==0) {
            Object o=Arrays.stream(clazz.getConstructors()).filter(a -> a.getParameters().length == 0).toList().get(0).newInstance();
            return o;
            } else {
                Constructor c= Arrays.stream(clazz.getConstructors()).filter(a -> a.isAnnotationPresent(Inject.class)).toList().get(0);
                Object[] parameters=constructParameters(c.getParameters());
                Object o=c.newInstance(parameters);

                Arrays.stream(clazz.getDeclaredMethods()).filter(a-> a.isAnnotationPresent(Inject.class)).forEach(a-> {
                    try {
                        a.invoke(o ,constructParameters(a.getParameters()));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (MultipleInjectConstructorsException e) {
                        e.printStackTrace();
                    } catch (NoConstructorFoundException e) {
                        e.printStackTrace();
                    } catch (MultipleAnnotationOnParameterException | InstantiationException e) {
                        e.printStackTrace();
                    }
                });
                Arrays.stream(clazz.getFields()).filter(a->a.isAnnotationPresent(Inject.class)).forEach(a-> {
                    try {
                        a.set(o,construct(a.getClass()));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (MultipleInjectConstructorsException e) {
                        e.printStackTrace();
                    } catch (NoConstructorFoundException e) {
                        e.printStackTrace();
                    } catch (MultipleAnnotationOnParameterException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    }
                });
                return o;
        }
    }

    private Object[] constructParameters(Parameter[] parameters) throws MultipleInjectConstructorsException, NoConstructorFoundException, MultipleAnnotationOnParameterException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Object[] constructed_parameters=new Object[parameters.length];
        int c=0;
        for (Parameter parameter:parameters) {
            if(parameter.isAnnotationPresent(Named.class) && nameBinds.containsKey(parameter.getName()))
               constructed_parameters[c]=construct(nameBinds.get(parameter.getName()));
            else if(parameter.getAnnotations().length>0 && !parameter.isAnnotationPresent(Named.class))
                constructed_parameters[c]=provides(parameter);
            else
                constructed_parameters[c]=construct(parameter.getClass());
            c++;
        }
    return parameters;
    }
    private Object provides(Parameter parameter){
        for (Class C=parameter.getAnnotations()[0].getClass();C!=Object.class ;C=C.getSuperclass()) {
            return Arrays.stream(C.getDeclaredMethods()).filter(a->a.isAnnotationPresent(Provides.class)).toList().get(0).getDefaultValue();
        }
        return null;
    }
}
