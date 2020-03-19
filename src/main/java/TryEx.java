import ij.ImageJ;

import io.scif.img.IO;
import io.scif.img.ImgIOException;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
/**
 * Opens a file with ImgOpener and threshold it based on a pre-defined value
 * All the pixels in the input image which are less than or equal to 180 are set to 0 (black) and
 * the pixels above 180 are set to 255 (white)
 * 
 * */
public class TryEx {


	public < T extends RealType< T > & NativeType< T > > TryEx() throws ImgIOException
	{
		
	  // open with ImgOpener
          final Img< T > img = (Img< T >) IO.openImgs( "DrosophilaWing.tif" ).get( 0 );
          ImageJFunctions.show( img );
        
          // Image Threshold function
          Img< T > output = imageThreshold( img );
 
          // display the copy
          ImageJFunctions.show( output );
		
	}
	
	public < T extends RealType< T > > Img< T > imageThreshold( final Img< T > input ) throws ImgIOException
        {
	   // create a new Image with the same properties
           // note that the input provides the size for the new image as it implements
           // the Interval interface
           Img< T > output = input.factory().create( input );
 
           // create a cursor for both images
           Cursor< T > cursorInput  = input.cursor();
           Cursor< T > cursorOutput = output.cursor();
	
	   // define the threshold	
           double threshold = 180.0;
        
	   // iterate over the input pixels
	   while ( cursorInput.hasNext())
	   {
	       // move both cursors forward by one pixel
	       cursorInput.fwd();
	       cursorOutput.fwd();
			
			
	       final RealType<?> thisPixel = cursorInput.get();
               
	       // check if the current pixel intensity is more than the threshold
               if (thisPixel.getRealDouble() > threshold)
               {
                   //set the current pixel to 255 in the input image
            	   thisPixel.setReal(255.0);
            	
            	   // set the value of this pixel of the output image
            	  cursorOutput.get().set( cursorInput.get() );
            	
               }
               else
               {
            	   //set the current pixel to 0 in the input image
            	   thisPixel.setReal(0.0);
            	
            	   // set the value of this pixel of the output image
            	   cursorOutput.get().set( cursorInput.get() );
               }
            
	   }
					
	   // return the output image
	   return output;
}
			
	public static void main(String[] args) 
	{
		// open an ImageJ window
		new ImageJ();

		// run the example
		new TryEx();

	}

}
