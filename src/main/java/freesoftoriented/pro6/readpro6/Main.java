package freesoftoriented.pro6.readpro6;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class Main implements ApplicationRunner {

	@Autowired
	private Pro6Editor editor;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		System.out.println("=== Pro6 Converter ===");
		Pro6ConverterArgs myargs = new Pro6ConverterArgs(args);
		System.out.println(myargs);
		if (myargs.isEnoughParameter()) {
			// 変換処理を実行
			editor.handleCommand(myargs.getPro6filepath().toString());
		} else {
			System.out.println("Please specify pro6 slide file.");
			System.out.println("Usage:");
			System.out.println("  to execute: java -jar pro6converter <filename>");
		}
	}

	/**
	 * 起動パラメータの解析処理
	 *
	 */
	@lombok.ToString
	@lombok.Getter
	private static class Pro6ConverterArgs {
		private boolean enoughParameter;
		private Path pro6filepath;

		public Pro6ConverterArgs(ApplicationArguments args) {
			enoughParameter = false;
			String[] sourceArgs = args.getSourceArgs();
			if (sourceArgs != null && sourceArgs.length > 0) {
				Path path = Paths.get(sourceArgs[0]);
				if (Files.exists(path) && Files.isReadable(path)) {
					enoughParameter = true;
					pro6filepath = path;
				}
			}
		}
	}

}
