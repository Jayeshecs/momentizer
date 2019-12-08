/**
 * 
 */
package momentizer;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * @author jayeshecs
 *
 */
public class MomentIndexer {

	private static final Logger logger = Logger.getLogger(MomentIndexer.class);

	private File workingDirectory = null;

	public MomentIndexer() {
		this(new File(".").getAbsoluteFile());
	}

	public MomentIndexer(File workingDirectory) {
		if (workingDirectory == null) {
			throw new IllegalArgumentException("Argument 'workingDirectory' cannot be null");
		}
		if (!workingDirectory.isDirectory()) {
			throw new IllegalArgumentException("Argument 'workingDirectory' must be a valid directory");
		}
		if (!workingDirectory.canRead() || !workingDirectory.canWrite() || !workingDirectory.canExecute()) {
			throw new IllegalArgumentException("Argument 'workingDirectory' must have read/write/execute permissions");
		}
		logger.info("Moment Indexer initialized with working directory - " + workingDirectory);
		this.workingDirectory = workingDirectory;
	}

	/**
	 * @return working directory
	 */
	public File getWorkingDirectory() {
		return workingDirectory;
	}

	public void startIndexing() {
		logger.info("Indexing started...");
		try {
			Directory directory = FSDirectory.open(new File(workingDirectory, "index").toPath());
			// create the indexer
			IndexWriterConfig conf = new IndexWriterConfig(new StandardAnalyzer());
			conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			final IndexWriter writer = new IndexWriter(directory, conf);
			final AtomicLong totalFiles = new AtomicLong();
			final AtomicLong countFolders = new AtomicLong();
			final AtomicLong countSkippedFiles = new AtomicLong();
			final AtomicLong countIndexedFiles = new AtomicLong();
			final AtomicLong countFailedFiles = new AtomicLong();
			new FileScanner(workingDirectory).scan(new FileScannerCallback() {
				
				public void folder(File folder) throws Exception {
					totalFiles.addAndGet(1);
					countFolders.addAndGet(1);
				}
				
				public void file(File file) throws Exception {
					totalFiles.addAndGet(1);
					String name = file.getName();
					int lastIndexOf = name.lastIndexOf('.');
					if (lastIndexOf < 0) {
						countSkippedFiles.addAndGet(1);
						// file without extension (skip it)
						return ;
					}
					String extension = name.substring(lastIndexOf + 1);
					if (!extension.toLowerCase().matches("(jpg|png|jpeg|gif)")) {
						countSkippedFiles.addAndGet(1);
						// skip all files that are not images
						return ;
					}
					Document document = new Document();
					document.add(new StringField("name", name, Field.Store.YES));
					String timeToString = DateTools.timeToString(file.lastModified(), DateTools.Resolution.MILLISECOND);
					document.add(new StringField("date", timeToString, Field.Store.YES));
					document.add(new StringField("size", String.valueOf(file.length()), Field.Store.YES));
					writer.addDocument(document);
					countIndexedFiles.addAndGet(1);
				}
				
				public void error(File file, Exception e) {
					countFailedFiles.addAndGet(1);
					logger.error("Exception occurred while scanning file/folder - " + file, e);
				}
			});
			writer.commit();
			writer.close();
			logger.info("Index Stats are as below:");
			logger.info(String.format("%10s | %10s | %10s | %10s | %10s ", "Total", "Indexed", "Folder", "Skipped", "Failed"));
			logger.info(String.format("%10i | %10i | %10i | %10i | %10i ", totalFiles.get(), countIndexedFiles.get(), countFolders.get(), countSkippedFiles.get(), countFailedFiles.get()));
			logger.info("Indexing completed.");
		} catch (IOException e) {
			logger.error("I/O error occurred while indexing at " + workingDirectory, e);
			logger.info("Indexing failed!");
		}
	}
	
	interface FileScannerCallback {
		void file(File file) throws Exception;
		
		void folder(File folder) throws Exception;
		
		void error(File file, Exception e);
	}
	
	class FileScanner {
		
		private File path;

		public FileScanner(File path) {
			this.path = path;
		}
		
		public void scan(FileScannerCallback callback) {
			scan(this.path.listFiles(), callback);
		}

		private void scan(File[] listFiles, FileScannerCallback callback) {
			if (callback == null) {
				logger.warn("No 'FileScannerCallback' provided and hence there is nothing to scan");
				return ;
			}
			if (listFiles == null || listFiles.length < 1) {
				return ;
			}
			for (File file : listFiles) {
				try {
					if (file.isFile()) {
						callback.file(file);
					} else if (file.isDirectory()) {
						callback.folder(file);
						scan(file.listFiles(), callback);
					}
				} catch (Exception e) {
					callback.error(file, e);
				}
			}
		}
	}

	public void findDuplicate() {

	}

}
