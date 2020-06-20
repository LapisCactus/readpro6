package freesoftoriented.pro6.readpro6.util;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class AopLogger {

	// AOPの場合は、publicメソッドしか対象にならない(さらに、SpringがDIするもののみ)
	// 戻り値 パッケージ..クラス名.メソッド名(引数)
	@Around("execution(* freesoftoriented..*.*(..))")
	public Object around(ProceedingJoinPoint pjp) throws Throwable {
		String className = pjp.getTarget().getClass().getSimpleName();
		String methodName = pjp.getSignature().getName();
		String args = Arrays.asList(pjp.getArgs()).toString();
		StdLog.debug("**[BEFORE] " + className + "." + methodName + "(" + args + ")");
		long methodStartTime = System.currentTimeMillis();
		Object ret;
		try {
			ret = pjp.proceed();
			return ret;
		} finally {
			long execTime = System.currentTimeMillis() - methodStartTime;
			StdLog.debug("**[AFTER] " + className + "." + methodName + " for " + execTime + " ms");
		}
	}

}
