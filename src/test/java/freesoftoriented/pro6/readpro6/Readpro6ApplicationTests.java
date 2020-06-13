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
	private ProPresentor6Data app;

	@Test
	public void test() {
		// app
		app.handleCommand("sample.xml");
	}

}
