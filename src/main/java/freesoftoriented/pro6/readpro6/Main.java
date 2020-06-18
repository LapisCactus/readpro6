package freesoftoriented.pro6.readpro6;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import freesoftoriented.pro6.readpro6.Pro6Editor.Options;
import freesoftoriented.pro6.readpro6.util.StdLog;

@Component
public class Main implements ApplicationRunner {

	@Autowired
	private Pro6Editor editor;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		StdLog.info("=== Pro6 Converter ===");
		Pro6ConverterArgs myargs = new Pro6ConverterArgs(args);
		StdLog.info(myargs.toString());
		if (myargs.isEnoughParameter()) {
			// 変換処理を実行
			List<Path> paths = myargs.getPro6filepath();
			for (Path path : paths) {
				String filename = path.toString();
				Options options = myargs.getOptions();
				editor.handleCommand(filename, options);
			}
		} else {
			StdLog.info("Please specify pro6 slide file.");
			StdLog.info("Usage:");
			StdLog.info("  java -jar readpro6.jar [<option>] <filename> [<other filenames> ...]");
			StdLog.info("  Available options are below:");
			StdLog.info("    --show-original-rtf ");
			StdLog.info("    --show-result-rtf ");
			StdLog.info("    --show-result-xml ");
			StdLog.info("    --bypass ");
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
		private List<Path> pro6filepath;
		private Pro6Editor.Options options = new Pro6Editor.Options(false, true, false, false);

		public Pro6ConverterArgs(ApplicationArguments args) {
			enoughParameter = false;
			// フラグなし引数はすべてファイルとして扱う
			List<String> sourceArgs = args.getNonOptionArgs();
			List<Path> paths = sourceArgs.stream().map(arg -> {
				Path path = Paths.get(arg);
				if (Files.exists(path) && Files.isReadable(path)) {
					return path;
				}
				return null;
			}).filter(item -> item != null).collect(Collectors.toList());
			if (paths.isEmpty() == false) {
				enoughParameter = true;
				pro6filepath = paths;
			}
			// オプションがあれば取り扱う
			Set<String> optionNames = args.getOptionNames();
			if (optionNames.contains("show-original-rtf")) {
				options.setLogOriginalRtf(true);
			}
			if (optionNames.contains("show-result-rtf")) {
				options.setLogConvertedRtf(true);
			}
			if (optionNames.contains("show-result-xml")) {
				options.setLogXml(true);
			}
			if (optionNames.contains("bypass")) {
				options.setEdit(false);
			}

		}
	}

}
