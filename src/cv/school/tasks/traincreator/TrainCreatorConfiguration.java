/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks.traincreator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.IOException;

/**
 *
 * @author roma2_000
 */
public class TrainCreatorConfiguration {
    private String initialVideo;
    private long toFps;
    private int bufferLength;
    private int rectWidth;
    private String trainDir;
    
    public void setInitialVideo(String video)   { this.initialVideo = video;    }
    public void setToFps(long fps)              { this.toFps = fps;             }
    public void setBufferLength(int len)        { this.bufferLength = len;      }
    public void setRectWidth(int wid)           { this.rectWidth = wid;         }
    public void setTrainDir(String dir)         { this.trainDir = dir;          }
    
    public String getInitialVideo()             { return this.initialVideo;     }
    public long getToFps()                      { return this.toFps;            }
    public int getBufferLength()                { return this.bufferLength;     }
    public int getRectWidth()                   { return this.rectWidth;        }
    public String getTrainDir()                 { return this.trainDir;         }
    
    public static TrainCreatorConfiguration readJSON(JsonParser parser) throws IOException {
        if (parser.nextToken() != JsonToken.START_OBJECT) {
            throw new IOException("Expected data to start with an Object");
        }
        TrainCreatorConfiguration result = new TrainCreatorConfiguration();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            parser.nextToken();
            
            if (fieldName.equals("initial_video")) {
                result.setInitialVideo(parser.getText());
            } else if (fieldName.equals("to_fps")) {
                result.setToFps(parser.getLongValue());
            } else if (fieldName.equals("buffer_length")) {
                result.setBufferLength(parser.getIntValue());
            } else if (fieldName.equals("rect_width")) {
                result.setRectWidth(parser.getIntValue());
            } else if (fieldName.equals("output_dir")) {
                result.setTrainDir(parser.getText());
            }
        }
        return result;
    }
}
