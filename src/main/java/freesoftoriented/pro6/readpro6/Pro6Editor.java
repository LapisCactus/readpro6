package freesoftoriented.pro6.readpro6;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import freesoftoriented.pro6.readpro6.ProPresentor6Data.RVDisplaySlide;
import freesoftoriented.pro6.readpro6.ProPresentor6Data.RVPresentationDocument;
import freesoftoriented.pro6.readpro6.ProPresentor6Data.RVSlideGrouping;
import freesoftoriented.pro6.readpro6.ProPresentor6Data.RVTextElement;

@Service
public class Pro6Editor {

	@Autowired
	private ProPresentor6Data pro6data;

	public void handleCommand(String filepath) {
		System.out.println("\nRead:" + filepath);
		// XML File→JavaObject
		System.out.println("\nConvert:");
		RVPresentationDocument document = pro6data.readFromFile(filepath);
		// Edit
		double sizefactor = Integer.parseInt(document.getWidth()) / 2215.0;
		System.out.println(
				String.format("\nSlide size: %s x %s (f%f)", document.getWidth(), document.getHeight(), sizefactor));
		List<RVSlideGrouping> slideGroup = document.findSlideGroup();
		for (RVSlideGrouping group : slideGroup) {
			List<RVDisplaySlide> slides = group.findDisplaySlide();
			System.out.println(String.format("Slide Group (%s slides)", slides.size()));
			for (RVDisplaySlide slide : slides) {
				List<RVTextElement> elements = slide.findTextElement();
				System.out.println(String.format("\n- Slide (%s text area)", elements.size()));
				for (int i = 0; i < elements.size(); i++) {
					RVTextElement element = elements.get(i);

					List<Integer> position = element.extractPosition();
					int ul_x = position.get(0);
					int ul_y = position.get(1);
					int lr_x = position.get(3);
					int lr_y = position.get(4);
					System.out.println(String.format("  - Text (%d,%d) w%d h%d", ul_x, ul_y, lr_x - ul_x, lr_y - ul_y));
					// 幅がスクリーンの90%未満の場合は、編集しない
					if (100 * Math.abs(lr_x - ul_x) / Integer.parseInt(document.getWidth()) < 90) {
						System.out.println("    This text area is not lyrics nor title. skipped.");
						continue;
					}
					// 矩形の上辺が画面の上部10%以内にない場合も、編集しない
					int uppery = Math.min(ul_y, lr_y);
					if (100 * uppery / Integer.parseInt(document.getHeight()) > 10) {
						System.out.println("    This text area is number? skipped.");
						continue;
					}
					// テキストエリアが１つの場合は、曲名スライド、そうでなければ歌詞スライドとして編集する
					String rawRTFData = element.findRawRTFData();
					RTFEditor rtf = new RTFEditor(rawRTFData);
					rtf.setFontSize((int) (sizefactor * 116 * 2));
					rtf.setFontSizeMillis((int) (sizefactor * 116 * 1000));
					rtf.updateColorTable();
					rtf.setFontColor(1);
					rtf.setStrokeWidth(100);
					rtf.setStrokeColor(2);
					rtf.setLeading(200);
					element.replaceRawRTFData(rtf.getRtfText());
					System.out.println(rtf.getRtfText());
				}
			}
		}

		// JavaObject→XML
		System.out.println("\n\nBack to XML:");
		pro6data.writeToFile(filepath + "_out.pro6", document);
		System.out.println("\nDONE");
	}

	/**
	 * RTF部分を編集する
	 *
	 */
	@lombok.ToString(exclude = { "rtfText" })
	public static class RTFEditor {

		@lombok.Getter
		private String rtfText;

		public RTFEditor(String rtftext) {
			this.rtfText = rtftext;
		}

		public void setFontSize(int size) {
			// フォントサイズはファイル内部では2倍になっている(double-point)
			updateCommandParam("\\fs", size);
		}

		public void setFontSizeMillis(int size) {
			// フォントサイズはファイル内部では1000倍になっている
			updateCommandParam("\\fsmilli", size);
		}

		public void setFontSize(int[] sizeArray) {
			updateCommandParams("\\fs", sizeArray);
		}

		public void setFontColor(int tableindex) {
			// カラーテーブルから選ぶ
			updateCommandParam("\\cf", tableindex);
			// updateCommandParam("\\strokec2", 2);
		}

		public void setStrokeWidth(int width) {
			// 枠線の幅は、内部では20倍
			updateCommandParam("\\strokewidth-", width);
		}

		public void setStrokeColor(int colorindex) {
			updateCommandParam("\\strokec", colorindex);
		}

		public void setLeading(int leading) {
			// 行間調整は、内部では20倍
			updateCommandParam("\\slleading-", leading);
		}

		public void updateColorTable() {
			// カラーテーブルを更新する
			replaceBlacket("{\\colortbl", "{\\colortbl;\\red255\\green255\\blue255;\\red25\\green25\\blue25;}");
			replaceBlacket("{\\*\\expandedcolortbl", "{\\*\\expandedcolortbl;;\\cssrgb\\c12984\\c12985\\c12984;}");
		}

		/**
		 * コマンドを探し、そのパラメータを変更する
		 * 
		 * @param command
		 * @param param
		 */
		private void updateCommandParam(String command, int param) {
			String[] segments = rtfText.split(command.replace("\\", "\\\\"));
			if (segments == null || segments.length == 0) {
				System.out.println("[INFO] nothing to do");
				return;
			}
			// コマンドで区切って、サイズ部分を上書き
			List<String> list = new ArrayList<>();
			list.add(segments[0]);
			Pattern pattern = Pattern.compile("([0-9]+)(.*)", Pattern.DOTALL);
			for (int i = 1; i < segments.length; i++) {
				Matcher matcher = pattern.matcher(segments[i]);
				if (!matcher.matches()) {
					System.out.println("[INFO] not apply this line for command:" + command);
					list.add(segments[i]);
					continue;
				}
				list.add(String.format("%d%s", param, matcher.group(2)));
			}
			rtfText = String.join(command, list);
		}

		/**
		 * コマンドを探し、そのパラメータを変更する
		 * 
		 * @param command
		 * @param param
		 */
		private void updateCommandParams(String command, int[] params) {
			String[] segments = rtfText.split(command.replace("\\", "\\\\"));
			if (segments == null || segments.length == 0) {
				System.out.println("[INFO] nothing to do");
				return;
			}
			// コマンドで区切って、サイズ部分を上書き
			List<String> list = new ArrayList<>();
			list.add(segments[0]);
			Pattern pattern = Pattern.compile("([0-9]+)(.*)", Pattern.DOTALL);
			for (int i = 1; i < segments.length; i++) {
				Matcher matcher = pattern.matcher(segments[i]);
				if (!matcher.matches()) {
					System.out.println("[INFO] not apply this line for command:" + command);
					list.add(segments[i]);
					continue;
				}
				list.add(String.format("%d%s", params[i], matcher.group(2)));
			}
			rtfText = String.join(command, list);
		}

		/**
		 * RTFのブラケットひとつをまるっと交換する(入れ子は対応しない)
		 * 
		 * @param startblacket ブラケット始まりを特定するための文字列
		 * @param replace      新しく取り込むブラケットまるごとの文字列
		 */
		private void replaceBlacket(String startblacket, String replace) {
			int startidx = rtfText.indexOf(startblacket);
			int endidx = rtfText.indexOf("}", startidx);
			String pre = rtfText.substring(0, startidx);
			String post = rtfText.substring(endidx + 1, rtfText.length());
//			System.out.println(pre + replace + post);
			rtfText = pre + replace + post;
		}
	}
}
