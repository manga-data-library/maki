package akb428.maki;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import twitter4j.FilterQuery;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import akb428.maki.config.Application;
import akb428.maki.dao.IMediaUrlDao;
import akb428.maki.dao.h2.MediaUrlDao;
import akb428.maki.model.HbaseConfModel;
import akb428.maki.model.MediaConfModel;
import akb428.maki.model.TwitterModel;
import akb428.maki.thread.MediaDownloderThread;
import akb428.util.Calender;

public class SearchMain {

    private static Logger logger = LoggerFactory.getLogger(SearchMain.class);

    public static void main(String[] args) throws ClassNotFoundException,
            JsonParseException, JsonMappingException, IOException, SQLException {

        // TODO 設定ファイルでMariaDBなどに切り替える
        // Class.forName("org.sqlite.JDBC");
        Class.forName("org.h2.Driver");

        TwitterModel twitterModel = null;
//        HbaseConfModel hbaseConfModel = null;
        MediaConfModel mediaConfModel = null;

        // パラメータチェック
        if (args.length == 0) {
            Application.load("conf/application.properties");
            twitterModel = TwitterConfParser.readConf("conf/twitter_conf.json");
        }
        else if (args.length == 1) {
            Application.load(args[0]);
            twitterModel = TwitterConfParser.readConf("conf/twitter_conf.json");
        }
        else if (args.length == 2) {
            Application.load(args[0]);
            twitterModel = TwitterConfParser.readConf(args[1]);
        }
        else {
            throw new IllegalArgumentException("ERROR:too many parameters!");
        }

        //Configuration conf = HBaseConfiguration.create();
        ApplicationConfParser applicationConfParser =
        		new ApplicationConfParser("./conf/application.json");
//        hbaseConfModel = applicationConfParser.getHbaseConfModel();
        mediaConfModel = applicationConfParser.getMediaConfModel();
/*
		if (hbaseConfModel.isExecute()) {
			List<String> resources = hbaseConfModel.getResource();
			for (String resource : resources) {
				conf.addResource(resource);
			}
		}*/

        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.setOAuthConsumer(twitterModel.getConsumerKey(),
                twitterModel.getConsumerSecret());
        twitterStream.setOAuthAccessToken(new AccessToken(twitterModel
                .getAccessToken(), twitterModel.getAccessToken_secret()));

        twitterStream.addListener(new MyStatusAdapter(applicationConfParser));
        ArrayList<String> track = new ArrayList<String>();
        track.addAll(Arrays.asList(Application.searchKeyword.split(",")));

        logger.info("number of search keywords:" + (Application.searchKeyword.split(",")).length);

        String[] trackArray = track.toArray(new String[track.size()]);


        if(Application.locations2DArray != null) {
            twitterStream.filter(new FilterQuery(0, null, trackArray, Application.locations2DArray));
        } else {
            // 400のキーワードが指定可能、５０００のフォローが指定可能、２５のロケーションが指定可能
            twitterStream.filter(new FilterQuery(0, null, trackArray));
        }

        if (mediaConfModel.isExecute()) {
            MediaDownloderThread mediaDownloderThread = new MediaDownloderThread();
            mediaDownloderThread.start();
        }
    }

}

class MyStatusAdapter extends StatusAdapter {

    private static Logger logger = LoggerFactory.getLogger(MyStatusAdapter.class);

    HbaseConfModel hbaseConfModel;
    MediaConfModel mediaConfModel;
    //Configuration hbaseConf;
    BufferedWriter bufferedWriter;
    FileOutputStream csv;

    long baseTime = System.currentTimeMillis(); // unixtime * 1000

    Calendar cal = Calendar.getInstance();
    int minute = cal.get(Calendar.MINUTE);
    int second = cal.get(Calendar.SECOND);
    int rotation = 3600000 - (minute * 60 * 1000) - (second * 1000);

    public MyStatusAdapter(ApplicationConfParser applicationConfParser) throws FileNotFoundException, UnsupportedEncodingException{
        //hbaseConfModel = applicationConfParser.getHbaseConfModel();
        mediaConfModel = applicationConfParser.getMediaConfModel();
        newBuffer();
    }

    public void newBuffer() throws FileNotFoundException, UnsupportedEncodingException{
        // 追記モード
        csv = new FileOutputStream("logs/" + Calender.yyyymmdd_hh() + ".csv"); // CSVデータファイル
        bufferedWriter
                = new BufferedWriter(new OutputStreamWriter(csv, "UTF-8"));
    }


    public void onStatus(Status status) {

		/*
		if (status.getGeoLocation() != null) {
			System.out.print(String.valueOf(status.getGeoLocation().getLatitude()));
			System.out.print(" ");
			System.out.print(String.valueOf(status.getGeoLocation().getLongitude()));
		}*/
        logger.info("  @" + status.getUser().getScreenName());
        logger.info(status.getText());

		/*
		if (hbaseConfModel.isExecute()) {
			// HBaseに登録する
			try {
				registHbase(status);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/
/*
		if (mediaConfModel.isExecute()) {
			registMediaUrl(status);
		}
		*/

        if (rotation <  System.currentTimeMillis() - baseTime ){
            rotation = 3600000;
            baseTime =  System.currentTimeMillis();
            try {
                bufferedWriter.close();
                csv.close();

                newBuffer();
            }
            catch (Exception e){
            	logger.error(e.getMessage(), e);
            }
        }

        writTwitterStreamToCSV(status);

    }


    public void writTwitterStreamToCSV(Status status) {
        try {
            bufferedWriter.write(status.getId()
                    + "," + StringEscapeUtils.escapeCsv(status.getUser().getScreenName())
                    + ","  + StringEscapeUtils.escapeCsv(status.getText())
                    + ","  + StringEscapeUtils.escapeCsv(status.getSource())
                    + ","  + status.getRetweetCount()
                    + ","  + status.getFavoriteCount()
                    + ","  + status.getCreatedAt()
            );

            if (status.getGeoLocation() != null) {
                bufferedWriter.write("," + String.valueOf(status.getGeoLocation().getLatitude()) // 緯度
                        + "," + String.valueOf(status.getGeoLocation().getLongitude()));//経度
            } else {
                bufferedWriter.write(",,");
            }

            MediaEntity[] arrMedia = status.getMediaEntities();

            for (MediaEntity media : arrMedia) {
                bufferedWriter.write("," + media.getMediaURL());
            }

            // mediaのMAXは4
            int blanknum = 4 - arrMedia.length;

            for (int i=1 ; i <= blanknum ; i++) {
                bufferedWriter.write(",");
            }

            bufferedWriter.write("," + String.valueOf(status.getCreatedAt().getTime() / 1000L));

            bufferedWriter.newLine();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

    }

    public void registMediaUrl(Status status) {
        // TODO 設定ファイルでMariaDBなどに切り替える
        IMediaUrlDao dao = new MediaUrlDao();

        MediaEntity[] arrMedia = status.getMediaEntities();

        if (arrMedia.length > 0) {
            logger.info("メディアURLが見つかりました");
        }
        for (MediaEntity media : arrMedia) {
            // http://kikutaro777.hatenablog.com/entry/2014/01/26/110350
            logger.info(media.getMediaURL());

            if (!dao.isExistUrl(media.getMediaURL())) {
                // TODO keywordを保存したいがここでは取得できないため一時的にtextをそのまま保存
                dao.registUrl(media.getMediaURL(), status.getText(), status
                        .getUser().getScreenName());
            }
        }
    }
/*
	public void registHbase(Status status) throws IOException {
		// TODO テーブルを作成するロジックをかく
		HTable table = new HTable(hbaseConf, "twitter_01");

		byte[] key = Bytes.toBytes(String.valueOf(status.getId()));
		byte[] family = Bytes.toBytes("data");
		Put p = new Put(key);
		p.add(family, Bytes.toBytes("Text"), Bytes.toBytes(status.getText()));
		p.add(family, Bytes.toBytes("ScreenName"),
				Bytes.toBytes(status.getUser().getScreenName()));
		p.add(family, Bytes.toBytes("Source"),
				Bytes.toBytes(status.getSource()));
		p.add(family, Bytes.toBytes("RetweetCount"),
				Bytes.toBytes(status.getRetweetCount()));
		p.add(family, Bytes.toBytes("FavoriteCount"),
				Bytes.toBytes(status.getFavoriteCount()));
		table.put(p);
	}*/
}
