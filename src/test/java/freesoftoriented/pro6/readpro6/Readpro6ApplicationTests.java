package freesoftoriented.pro6.readpro6;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class Readpro6ApplicationTests {

	@Test
	void contextLoads() {
	}

	@Autowired
	private Pro6Editor app;

	@Test
	public void test() {
		// app
		app.handleCommand("inochino.pro6", new Pro6Editor.Options(false, false, false, false, ".\\out"));
		// app.handleCommand("687-638.pro6");
	}

}
