package ru.vyarus.java.generics.resolver

import ru.vyarus.java.generics.resolver.context.container.GenericArrayTypeImpl
import ru.vyarus.java.generics.resolver.context.container.ParameterizedTypeImpl
import ru.vyarus.java.generics.resolver.context.container.WildcardTypeImpl
import ru.vyarus.java.generics.resolver.support.Base1
import ru.vyarus.java.generics.resolver.support.Root
import ru.vyarus.java.generics.resolver.util.TypeUtils
import spock.lang.Specification

import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

/**
 * @author Vyacheslav Rusakov
 * @since 11.05.2018
 */
class TypesCompatibilityTest extends Specification {

    def "Check types compatibility"() {

        expect:
        TypeUtils.isCompatible(type1, type2) == res

        where:
        type1                          | type2                       | res
        String                         | Integer                     | false
        Object                         | Integer                     | true
        String                         | Object                      | true
        Base1                          | Root                        | true
        Root                           | Base1                       | true
        param(List, String)            | param(List, Integer)        | false
        param(List, Base1)             | param(List, Root)           | true
        param(List, Root)              | param(List, Base1)          | true
        array(String)                  | array(Integer)              | false
        array(Base1)                   | array(Root)                 | true
        array(Integer)                 | String                      | false
        String                         | array(Integer)              | false
        param(List, String)            | param(ArrayList, Integer)   | false
        param(ArrayList, String)       | param(List, Integer)        | false
        param(ArrayList, Base1)        | param(List, Root)           | true
        array(param(List, String))     | array(param(List, Integer)) | false
        array(param(ArrayList, Base1)) | array(param(List, Root))    | true
        new String[0].class            | new Integer[0].class        | false
        new Base1[0].class             | new Root[0].class           | true
        lower(String)                  | lower(String)               | true
        lower(String)                  | lower(Integer)              | false
        lower(Number)                  | lower(Integer)              | true
        lower(String)                  | String                      | true
        String                         | lower(String)               | true
        lower(Number)                  | Integer                     | false
        Integer                        | lower(Number)               | false
        lower(Integer)                 | upper(Number, Comparable)   | true
        upper(Number, Comparable)      | lower(Integer)              | true
        lower(Number)                  | Object                      | true
        lower(Object)                  | String                      | true
        String                         | lower(Object)               | true
        Object                         | lower(Number)               | true
        upper(Number, Comparable)      | Integer                     | true
        Integer                        | upper(Number, Comparable)   | true
        upper(Number, Comparable)      | String                      | false
        String                         | upper(Number, Comparable)   | false
    }

    def "Check types comparison"() {
        expect:
        TypeUtils.isMoreSpecific(type1, type2) == res

        where:
        type1                     | type2                     | res
        Base1                     | Root                      | false
        Root                      | Base1                     | true
        param(List, String)       | param(List, Object)       | true
        param(List, Base1)        | param(List, Root)         | false
        array(Base1)              | array(Root)               | false
        array(Root)               | array(Base1)              | true
        param(List, String)       | param(ArrayList, String)  | false
        param(ArrayList, String)  | param(List, String)       | true
        param(ArrayList, Object)  | param(ArrayList, String)  | false
        new Base1[0].class        | new Root[0].class         | false
        new Root[0].class         | new Base1[0].class        | true
        lower(String)             | lower(String)             | true
        lower(Number)             | lower(Integer)            | true
        lower(String)             | String                    | false
        String                    | lower(String)             | true
        lower(Integer)            | upper(Number, Comparable) | false
        upper(Number, Comparable) | lower(Integer)            | true
        lower(Number)             | Object                    | true
        lower(Object)             | String                    | false
        String                    | lower(Object)             | true
        Object                    | lower(Number)             | false
        upper(Number, Comparable) | Integer                   | false
        Integer                   | upper(Number, Comparable) | true
    }

    def "Check type comparison failure"() {

        when: "compare incopatible types"
        TypeUtils.isMoreSpecific(String, Integer)
        then: "err"
        def ex = thrown(IllegalArgumentException)
        ex.message == "Type String can't be compared to Integer because they are not compatible"
    }

    def "Check specific type resolution"() {

        expect:
        TypeUtils.getMoreSpecificType(Base1, Root) == Root
    }

    def "Check incompatible bounds comparison"() {

        when: "bad bound provided"
        TypeUtils.isAssignableBounds([Class] as Class[], [] as Class[])
        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Incomplete bounds information: [class java.lang.Class] []"
    }

    ParameterizedType param(Class root, Type... types) {
        return new ParameterizedTypeImpl(root, types)
    }

    GenericArrayType array(Type type) {
        return new GenericArrayTypeImpl(type)
    }

    WildcardType upper(Type... types) {
        return WildcardTypeImpl.upper(types)
    }

    WildcardType lower(Type type) {
        return WildcardTypeImpl.lower(type)
    }
}
