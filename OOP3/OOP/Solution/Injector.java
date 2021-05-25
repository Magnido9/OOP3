package OOP.Solution;

import OOP.Provided.IllegalBindException;
import OOP.Provided.MultipleAnnotationOnParameterException;
import OOP.Provided.MultipleInjectConstructorsException;
import OOP.Provided.NoConstructorFoundException;

import javax.management.ObjectInstance;
import java.lang.annotation.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Supplier;


public class Injector {
    HashMap<Class,Class> classBinds;
    HashMap<String,Class> nameBinds;
    HashMap<Class,Object> instanceBinds;
    HashMap<Class,Supplier> supplierBinds;
    public Injector(){
        classBinds=new HashMap<Class,Class>();
        nameBinds=new HashMap<String,Class>();
        instanceBinds=new HashMap<Class,Object>();
        supplierBinds=new HashMap<Class,Supplier>();
    }
    public void bind(Class clazz1, Class clazz2) throws IllegalBindException {
        if (clazz1.isAssignableFrom(clazz2)){
            classBinds.put(clazz1, clazz2);
            instanceBinds.remove(clazz1);
            supplierBinds.remove(clazz1);
    }
    else
            throw new IllegalBindException();
    }
    public void bindToInstance(Class clazz1, Object instance) throws IllegalBindException{
        if(instance==null)
            throw new IllegalBindException();
        if(clazz1.isAssignableFrom(instance.getClass())){
            instanceBinds.put(clazz1,instance);
            classBinds.remove(clazz1);
            supplierBinds.remove(clazz1);
        }
        else
            throw new IllegalBindException();
    }
    public void bindByName( String clazz2,Class clazz1) throws IllegalBindException{
        nameBinds.put(clazz2,clazz1);
    }
    public void bindToSupplier(Class clazz1, Supplier clazz2) throws IllegalBindException{
        supplierBinds.put(clazz1,clazz2);
        classBinds.remove(clazz1);
        instanceBinds.remove(clazz1);
        }
    public Object construct(Class clazz) throws MultipleInjectConstructorsException, NoConstructorFoundException, MultipleAnnotationOnParameterException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if(instanceBinds.containsKey(clazz))
            return instanceBinds.get(clazz);
        else if(classBinds.containsKey(clazz))
            return construct(classBinds.get(clazz));
        else if(supplierBinds.containsKey(clazz))
            return supplierBinds.get(clazz).get();
        else if(Arrays.stream(clazz.getConstructors()).filter(a->a.isAnnotationPresent(Inject.class)).toArray().length>1)
            throw new MultipleInjectConstructorsException();
        else if(Arrays.stream(clazz.getConstructors()).filter(a->a.isAnnotationPresent(Inject.class)).toArray().length==0) {
            if(Arrays.stream(clazz.getConstructors()).filter(a -> a.getParameters().length == 0).toArray().length==0)
                throw new NoConstructorFoundException();
            Constructor m=Arrays.stream(clazz.getConstructors()).filter(a -> a.getParameters().length == 0).toList().get(0);
            m.setAccessible(true);
            Object o=m.newInstance();
            return o;
            } else {
                Constructor c= Arrays.stream(clazz.getConstructors()).filter(a -> a.isAnnotationPresent(Inject.class)).toList().get(0);
                Object[] parameters=constructParameters(c.getParameters());
                c.setAccessible(true);
                Object o=c.newInstance(parameters);

                Arrays.stream(clazz.getDeclaredMethods()).filter(a-> a.isAnnotationPresent(Inject.class)).forEach(a-> {
                    try {
                        a.setAccessible(true);
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
        Named na;
        int c=0;
        for (Parameter parameter:parameters) {
            if(parameter.isAnnotationPresent(Named.class) && nameBinds.containsKey(parameter.getName())){
                na=(Named) Arrays.stream(parameter.getAnnotations()).filter(a->a.annotationType()==Named.class).toList().get(0);
                constructed_parameters[c]=construct(nameBinds.get(na.value()));
            }
            else if(parameter.getAnnotations().length>0 && !parameter.isAnnotationPresent(Named.class))
                constructed_parameters[c]=provides(parameter, Arrays.stream(parameter.getAnnotations()).toList().get(0));
            else
                constructed_parameters[c]=construct(parameter.getClass());
            c++;
        }
    return constructed_parameters;
    }
    private Object provides(Parameter parameter,Annotation n) throws InvocationTargetException, IllegalAccessException {
        for (Class C=this.getClass();C!=Object.class ;C=C.getSuperclass()) {
            if(Arrays.stream(C.getDeclaredMethods()).filter(a->a.isAnnotationPresent(Provides.class)).toList().size()!=0 && Arrays.stream(C.getDeclaredMethods()).filter(a->a.isAnnotationPresent(n.annotationType())).toList().size()!=0) {
                Method m2=Arrays.stream(C.getDeclaredMethods()).filter(a -> a.isAnnotationPresent(Provides.class) && a.isAnnotationPresent(n.annotationType())).toList().get(0);
                m2.setAccessible(true);
                Object b=m2.invoke(this);
                return b;
            }
        }
        return null;
    }
}
