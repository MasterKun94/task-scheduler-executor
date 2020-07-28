package com.oceanum.pluggable;

import java.lang.reflect.Method;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws Throwable {
        String mainClass = args[0];
        String[] argz = Arrays.copyOfRange(args, 1, args.length);
        Class<?> clazz = Class.forName(mainClass);
        Method method = clazz.getMethod("main", String[].class);
        method.invoke(null, (Object) argz);
    }
}
