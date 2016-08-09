package jackplay.play;

import jackplay.bootstrap.PlayGround;
import jackplay.bootstrap.TraceKeeper;
import jackplay.bootstrap.TraceLog;
import jackplay.javassist.ClassPool;
import jackplay.javassist.CtClass;
import jackplay.javassist.CtMethod;
import jackplay.javassist.NotFoundException;

import java.lang.instrument.Instrumentation;
import java.text.SimpleDateFormat;
import java.util.*;

import static jackplay.javassist.bytecode.AccessFlag.NATIVE;

public class InfoCenter {

    public static CtMethod locateMethod(PlayGround playGround, String methodFullName, String methodShortName) throws NotFoundException {
        ClassPool cp = ClassPool.getDefault();
        CtMethod found = null;
        try {
            CtClass cc = cp.get(playGround.classFullName);

            CtMethod[] methods = cc.getDeclaredMethods(methodShortName);
            for (CtMethod m : methods) {
                if (m.getLongName().equals(methodFullName)) {
                    found = m;
                }
            }
        } catch(NotFoundException nfe) {
        }

        if (null == found) {
            throw new NotFoundException(playGround.methodFullName + " not found!");
        } else {
            return found;
        }
    }

    static class ClassComparator implements Comparator<Class> {
        public int compare(Class o1, Class o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    static class MethodComparator implements Comparator<CtMethod> {
        public int compare(CtMethod o1, CtMethod o2) {
            return o1.getLongName().compareTo(o2.getLongName());
        }
    }

    Instrumentation inst;
    ProgramManager pm;
    final static ClassComparator classComparator = new ClassComparator();
    final static MethodComparator methodComparator = new MethodComparator();

    public List<Map<String, String>> getLoadedMethods() throws Exception {
        List<Map<String, String>> loadedMethods = new ArrayList<>();
        List<CtClass> classes = modifiableClasses();

        for (CtClass clazz : classes) {
            CtMethod[] methods = clazz.getDeclaredMethods();
            Arrays.sort(methods, methodComparator);
            for (CtMethod m : methods) {
                if ((m.getMethodInfo().getAccessFlags() & NATIVE) == NATIVE) continue;

                PlayGround pg = new PlayGround(m.getLongName());
                Map<String, String> loadedMethod = new HashMap<>();
                loadedMethod.put("classFullName", pg.classFullName);
                loadedMethod.put("methodFullName", pg.methodFullName);
                loadedMethod.put("methodLongName", pg.methodLongName);
                loadedMethod.put("returnType", m.getReturnType().getName());

                loadedMethods.add(loadedMethod);
            }
        }

        return loadedMethods;
    }


    private List<CtClass> modifiableClasses() throws Exception {
        List<CtClass> modifiableClasses = new ArrayList<CtClass>();
        Class[] classes = inst.getAllLoadedClasses();
        Arrays.sort(classes, classComparator);

        ClassPool cp = ClassPool.getDefault();

        for (Class clazz : classes) {
            if (inst.isModifiableClass(clazz) && isClassOfInterest(clazz)) {

                try {
                    CtClass cc = cp.get(clazz.getName());
                    modifiableClasses.add(cc);
                } catch(NotFoundException nfe) {
                }
            }
        }

        return modifiableClasses;
    }

    private boolean isClassOfInterest(Class clazz) {
        return !clazz.isInterface()
                && !clazz.isAnnotation()
                && !clazz.isArray()
                && !clazz.getName().startsWith("java.")
                && !clazz.getName().startsWith("jdk.internal.")
                && !clazz.getName().startsWith("sun.")
                && !clazz.getName().startsWith("com.sun.")
                && !clazz.getName().startsWith("jackplay.");
    }

    public void init(Instrumentation inst, ProgramManager pm) {
        this.inst = inst;
        this.pm = pm;
    }

    public List<Map<String, Object>> getTraceLogs() {
        Iterator<TraceLog> it = TraceKeeper.copyTraceLogs().iterator();
        List<Map<String, Object>> listOfLogs = new ArrayList<>();

        while (it.hasNext()) {
            try {
                TraceLog traceLog = it.next();

                Map<String, Object> map = new HashMap<>();
                map.put("when", formatDate(traceLog.when));
                map.put("tracePoint", traceLog.tracePoint.toString());
                map.put("classFullName", traceLog.pg.classFullName);
                map.put("methodShortName", traceLog.pg.methodShortName);
                map.put("uuid", traceLog.uuid);
                map.put("arguments", traceLog.arguments);
                map.put("returnedValue", traceLog.returnedValue);
                map.put("exceptionStackTrace", traceLog.exceptionStackTrace);
                map.put("elapsed", traceLog.elapsed);
                map.put("argsLen", traceLog.argsLen);

                listOfLogs.add(map);
            } catch(ConcurrentModificationException e) {}
        }

        return listOfLogs;
    }

    static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static String formatDate(Date when) {
        return formatter.format(when);
    }

}
