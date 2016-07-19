package jackplay.play.domain;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.util.Objects;

public class PlayGround {
    public final String className;
    public final String methodLongName;
    public final String methodShortName;

    final static String INVALID_MESSAGE = "invalid format, correct format is className.methodName()";

    public PlayGround(String methodLongName) {
        if (null == methodLongName
                || methodLongName.length() < 5
                || !methodLongName.endsWith(")")
                || methodLongName.indexOf(' ') >= 0) {
            throwInvalidFormatMessage(methodLongName);
        }

        int lastDot = methodLongName.lastIndexOf('.');
        if (lastDot <= 1 || methodLongName.endsWith(".")) throwInvalidFormatMessage(methodLongName);

        int firstParen = methodLongName.indexOf('(');
        if (firstParen <= 0) throwInvalidFormatMessage(methodLongName);

        int dotBeforeMethodName = methodLongName.substring(0, firstParen).lastIndexOf('.');

        this.className = methodLongName.substring(0, dotBeforeMethodName);
        this.methodLongName = methodLongName;
        this.methodShortName = findMethodShortName(methodLongName);
    }

    private void throwInvalidFormatMessage(String methodLongName) {
        throw new RuntimeException("[" + methodLongName + "] is " + INVALID_MESSAGE);
    }

    private static String findMethodShortName(String methodLongName) {
        int firstParen = methodLongName.indexOf('(');
        int dotBeforeMethodName = methodLongName.substring(0, firstParen).lastIndexOf('.');
        return methodLongName.substring(dotBeforeMethodName + 1, firstParen);
    }


    public CtMethod locateMethod() throws NotFoundException {
        ClassPool cp = ClassPool.getDefault();
        CtMethod found = null;
        try {
            CtClass cc = cp.get(this.className);

            CtMethod[] methods = cc.getDeclaredMethods(methodShortName);
            for (CtMethod m : methods) {
                if (m.getLongName().equals(methodLongName)) {
                    found = m;
                }
            }
        } catch(NotFoundException nfe) {
        }

        if (null == found) {
            throw new NotFoundException(this.methodLongName + " not found!");
        } else {
            return found;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayGround playGround = (PlayGround) o;
        return Objects.equals(className, playGround.className) &&
                Objects.equals(methodLongName, playGround.methodLongName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methodLongName);
    }

    @Override
    public String toString() {
        return "methodLongName";
    }
}