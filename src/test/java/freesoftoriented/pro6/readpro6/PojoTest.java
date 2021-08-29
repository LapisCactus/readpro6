package freesoftoriented.pro6.readpro6;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

public class PojoTest {

	@Test
	public void test() {
		Pro6Editor.Options opts = new Pro6Editor.Options();

		String filename = "\\pro6test\\me\\test.pro6";
		opts.setOutputFolder(null);
		Path out1 = opts.getOutputPathDirectoryEnsured(filename);
		System.out.println(out1);
		Path out1a = opts.getOutputPathDirectoryEnsured(filename + ".txt");
		System.out.println(out1a);

		opts.setOutputFolder("\\pro6test\\me\\outputdir\\");
		Path out2 = opts.getOutputPathDirectoryEnsured(filename);
		System.out.println(out2);
		Path out2a = opts.getOutputPathDirectoryEnsured(filename + ".txt");
		System.out.println(out2a);

		filename = "/pro6test/me/thisispro6";
		opts.setOutputFolder(null);
		out1 = opts.getOutputPathDirectoryEnsured(filename);
		System.out.println(out1);
		out1a = opts.getOutputPathDirectoryEnsured(filename + ".txt");
		System.out.println(out1a);

		opts.setOutputFolder("outputdir");
		out2 = opts.getOutputPathDirectoryEnsured(filename);
		System.out.println(out2);
		out2a = opts.getOutputPathDirectoryEnsured(filename + ".txt");
		System.out.println(out2a);
	}
}
