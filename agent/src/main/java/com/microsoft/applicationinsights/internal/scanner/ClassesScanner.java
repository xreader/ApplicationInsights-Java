package com.microsoft.applicationinsights.internal.scanner;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by gupele on 5/20/2015.
 */
public class ClassesScanner {
    public HashMap<String, ArrayList<String>> data = new HashMap<String, ArrayList<String>>();

    void add(String superName, String className) {
        ArrayList<String> n = data.get(superName);
        if (n == null) {
            n = new ArrayList<String>();
            data.put(superName, n);
//            System.out.println("add: " + superName);
        }
        n.add(className);
    }

    public List<String> scanForClasses(String packageToScan) {
        final ArrayList<String> performanceModuleNames = new ArrayList<String>();
        SuperFinder.TypeReporter reporter = new SuperFinder.TypeReporter() {
            @Override
            @SuppressWarnings("unchecked")
            public Class<? extends Annotation>[] annotations() {
                return new Class[]{SuppressWarnings.class};
            }

            @Override
            public void notify(String className, String superName, Set<String> interfaceNames) {
//                if (!"java/lang/Object".equals(superName)) {
//                    add(superName, className);
//                }
//                System.out.println(className);
//                System.out.println("super :    " + superName);
//                if (interfaceNames != null) {
//                    for (String i : interfaceNames) {
//                        add(i, className);
//                        System.out.println("inter    : " + i);
//                    }
//                }
            }
        };


        final SuperFinder annotationDetector = new SuperFinder(reporter);
        try {
            annotationDetector.detect(packageToScan);

//            int i = 0;
//            printLevel(i, "java/sql/Statement");
//            ArrayList<String> n = data.get("java/sql/Statement");
//            ++i;
//            while (n != null && !n.isEmpty()) {
//                for (String name : n) {
//                    print(i, name);
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return performanceModuleNames;
    }

    private void printLevel(int spaces, String className) {
        print(spaces, className);
        ArrayList<String> names = data.get(className);

        if (names == null) {
            return;
        }

        for (String name : names) {
            printLevel(spaces + 1, name);
        }
    }


    private void print(int spaces, String name) {
        for (int i = 0; i < spaces; ++i) {
            System.out.print(" ");
        }
        System.out.println(name);
    }
}
