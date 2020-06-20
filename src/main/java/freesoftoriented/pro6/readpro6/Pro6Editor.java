package freesoftoriented.pro6.readpro6;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import freesoftoriented.pro6.readpro6.ProPresentor6Data.RVDisplaySlide;
import freesoftoriented.pro6.readpro6.ProPresentor6Data.RVPresentationDocument;
import freesoftoriented.pro6.readpro6.ProPresentor6Data.RVSlideGrouping;
import freesoftoriented.pro6.readpro6.ProPresentor6Data.RVTextElement;
import freesoftoriented.pro6.readpro6.util.StdLog;

/**
 * Pro6スライドファイルを編集する
 *
 */
@Service
public class Pro6Editor {

	/**
	 * コマンドで指定されたファイルを処理する
	 * 
	 * @param filepath
	 * @param opt
	 */
	public void handleCommand(String filepath, Options opt) {
		// XML File→JavaObject
		RVPresentationDocument document = ProPresentor6Data.readFromFile(filepath);
		// Edit
		String slideWidth = document.getWidth();
		String slideHeight = document.getHeight();
		double sizefactor = Integer.parseInt(slideWidth) / 2215.0;
		StdLog.info(String.format("[File:%s %sx%s (f%f)]", filepath, slideWidth, slideHeight, sizefactor));
		List<RVSlideGrouping> slideGroup = document.findSlideGroup();
		if (slideGroup == null) {
			return;
		}
		for (RVSlideGrouping group : slideGroup) {
			List<RVDisplaySlide> slides = group.findDisplaySlide();
			if (slides == null) {
				continue;
			}
			StdLog.info(String.format("- [Group: %s slides]", slides.size()));
			for (RVDisplaySlide slide : slides) {
				List<RVTextElement> elements = slide.findTextElement();
				if (elements == null) {
					continue;
				}
				StdLog.info(String.format(" - [Slide: %s text area]", elements.size()));
				for (int i = 0; i < elements.size(); i++) {
					RVTextElement element = elements.get(i);

					List<Integer> position = element.extractPosition();
					int ul_x = position.get(0);
					int ul_y = position.get(1);
					int lr_x = position.get(3);
					int lr_y = position.get(4);
					StdLog.info(String.format("  - [Text: at(%d,%d) %dx%d", ul_x, ul_y, lr_x - ul_x, lr_y - ul_y));
					String rawRTFData = element.findRawRTFData();
					RTFEditor rtf = new RTFEditor(rawRTFData);
					if (opt.isLogOriginalRtf()) {
						StdLog.info(rtf.getRtfText());
					}

					// 編集不要ならここで抜ける
					if (!opt.isEdit()) {
						continue;
					}
					// 幅がスクリーンの90%未満の場合は、編集しない
					if (100 * Math.abs(lr_x - ul_x) / Integer.parseInt(document.getWidth()) < 90) {
						StdLog.info("    This text area is not lyrics nor title. skipped.");
						continue;
					}
					// 矩形の上辺が画面の上部10%以内にない場合も、編集しない
					int uppery = Math.min(ul_y, lr_y);
					if (100 * uppery / Integer.parseInt(document.getHeight()) > 10) {
						StdLog.info("    This text area is index? skipped.");
						continue;
					}
					// タイトルも含め、すべて歌詞スライドとして編集する
					rtf.setFontSize((int) (sizefactor * 116 * 2));
					rtf.setFontSizeMillis((int) (sizefactor * 116 * 1000));
					rtf.updateColorTable();
					rtf.setFontColor(1);
					rtf.setStrokeWidth(100);
					rtf.setStrokeColor(2);
					rtf.setLeading(200);
					element.replaceRawRTFData(rtf.getRtfText());
					if (opt.isLogConvertedRtf()) {
						StdLog.info(rtf.getRtfText());
					}
				}
			}
		}

		// JavaObject→XML
		if (opt.isLogXml()) {
			StdLog.info("");
			StdLog.info("Result XML:");
			ProPresentor6Data.dump(document);
		}

		if (opt.getOutputFolder() == null) {
			ProPresentor6Data.writeToFile(filepath + "_out.pro6", document);
		} else {
			Path outputDir = Paths.get(opt.getOutputFolder());
			if (!Files.exists(outputDir)) {
				try {
					Files.createDirectories(outputDir);
				} catch (IOException e) {
					// !?
					StdLog.error("Output directory cannot created!");
					return;
				}
			}
			Path pro6file = Paths.get(filepath);
			Path outputFile = outputDir.resolve(pro6file.getFileName());
			ProPresentor6Data.writeToFile(outputFile.toString(), document);
		}
		StdLog.info("DONE");
		StdLog.info("");
	}

	/**
	 * 変換処理のオプション
	 *
	 */
	@lombok.Data
	@lombok.NoArgsConstructor
	@lombok.AllArgsConstructor
	public static class Options {

		public static Options DEFAULT = new Options();

		/** 変換前のRTFを表示 */
		private boolean logOriginalRtf = false;

		/** 変換を実施する */
		private boolean edit = true;

		/** 変換後のRTFを表示 */
		private boolean logConvertedRtf = false;

		/** 変換後のXMLを表示 */
		private boolean logXml = false;

		/** 変換後のXMLを保存するディレクトリ. 未指定で、同一フォルダにリネーム保存. */
		private String outputFolder = null;

	}

	/**
	 * RTF部分を編集する
	 * <p>
	 * 参考： <a href=
	 * "https://developer.apple.com/library/archive/documentation/Cocoa/Conceptual/AttributedStrings/Tasks/RTFAndAttrStrings.html">Attributed
	 * String Programming Guide </a>
	 * </p>
	 */
	@lombok.ToString(exclude = { "rtfText" })
	public static class RTFEditor {

		@lombok.Getter
		private String rtfText;

		public RTFEditor(String rtftext) {
			this.rtfText = rtftext;
		}

		public void setFontSize(int size) {
			// フォントサイズはファイル内部では2倍になっている(half-point value)
			updateCommandParam("\\fs", size);
		}

		public void setFontSizeMillis(int size) {
			// フォントサイズ(1000 * font size)
			updateCommandParam("\\fsmilli", size);
		}

		public void setFontSize(int[] sizeArray) {
			updateCommandParams("\\fs", sizeArray);
		}

		public void setFontColor(int tableindex) {
			// カラーテーブルから選ぶ
			updateCommandParam("\\cf", tableindex);
		}

		public void setStrokeWidth(int width) {
			// 枠線の幅は、内部では20倍(単位：twip)
			updateCommandParam("\\strokewidth", width);
		}

		public void setStrokeColor(int colorindex) {
			updateCommandParam("\\strokec", colorindex);
		}

		public void setLeading(int leading) {
			// 行間調整は、内部では20倍(単位：twip)
			updateCommandParam("\\slleading", leading);
		}

		public void updateColorTable() {
			// カラーテーブルを更新する
			replaceBlacket("{\\colortbl", "{\\colortbl;\\red255\\green255\\blue255;\\red25\\green25\\blue25;}");
			replaceBlacket("{\\*\\expandedcolortbl", "{\\*\\expandedcolortbl;;\\cssrgb\\c12984\\c12985\\c12984;}");
		}

		/**
		 * コマンドを探し、そのパラメータを変更する
		 * 
		 * @param command コマンド文字列
		 * @param param
		 */
		private void updateCommandParam(String command, int param) {
			String[] segments = rtfText.split(command.replace("\\", "\\\\"));
			if (segments == null || segments.length == 0) {
				StdLog.debug("nothing to do");
				return;
			}
			// コマンドで区切って、サイズ部分を上書き
			List<String> list = new ArrayList<>();
			list.add(segments[0]);
			Pattern pattern = Pattern.compile("([-]?)([0-9]+)(.*)", Pattern.DOTALL);
			for (int i = 1; i < segments.length; i++) {
				Matcher matcher = pattern.matcher(segments[i]);
				if (!matcher.matches()) {
					StdLog.debug("not apply this line for command:" + command);
					list.add(segments[i]);
					continue;
				}
				list.add(String.format("%s%d%s", matcher.group(1), param, matcher.group(3)));
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
				StdLog.debug("nothing to do");
				return;
			}
			// コマンドで区切って、サイズ部分を上書き
			List<String> list = new ArrayList<>();
			list.add(segments[0]);
			Pattern pattern = Pattern.compile("([0-9]+)(.*)", Pattern.DOTALL);
			for (int i = 1; i < segments.length; i++) {
				Matcher matcher = pattern.matcher(segments[i]);
				if (!matcher.matches()) {
					StdLog.debug("not apply this line for command:" + command);
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
			StdLog.debug(pre + replace + post);
			rtfText = pre + replace + post;
		}
	}
}
