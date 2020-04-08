import ij.ImageJ;
 
import io.scif.img.IO;
import io.scif.img.ImgIOException;
import mpicbg.util.RealSum;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

/**
 * This example applies adaptive thresholding to an image 
 * Each pixel is compared to the average of the surrounding pixels
 * Compute the average of an s x s window of pixels centered around each pixel
 * All the pixels in the input image which are less than or equal to average is set to 0 (black) and
 * the pixels above the average are set to 255 (white). Window size is 3x3.
 * 
 * */

public class AdaptiveThreshold 
{

	public < T extends RealType< T > & NativeType< T > > AdaptiveThreshold() throws ImgIOException
	{
		// open with SCIFIO as a IntType
        Img< IntType > img = IO.openImgs( "bradley_method_01.png", new IntType() ).get( 0 );
		
        ImageJFunctions.show( img );
		
        //find the adaptive threshold - mean of surrounding pixels
        AdaptiveThreshold( img );
        
        // Display the final image
        ImageJFunctions.show( img );
        	
     }
        
    //@SuppressWarnings("null")
    public < T extends Comparable< T >, U extends RealType< U > > 
    void AdaptiveThreshold(RandomAccessibleInterval< IntType > source)
    throws ImgIOException
    {
    // define an interval that is one pixel smaller on each side in each dimension,
    // so that the search in the 8-neighborhood (3x3x3...x3) never goes outside
    // of the defined interval
    Interval interval = Intervals.expand( source, -1 );

    // create a view on the source with this interval
    source = Views.interval( source, interval );

    // instantiate a RectangleShape to access rectangular local neighborhoods
    // of radius 1 (that is 3x3x...x3 neighborhoods)
    final RectangleShape shape = new RectangleShape( 1, false );

    // iterate over the set of neighborhoods in the image
    for ( final Neighborhood< IntType > localNeighborhood : shape.neighborhoods( source ) )
    {
        
        final RealSum realSum = new RealSum();
        long count = 0;
        double avg = 0 ;
        

        // find the average of all pixels in the local neighborhood
        for ( final IntType value : localNeighborhood )
        {
            	realSum.add(value.get() );
            	++count;
            
        }
        avg =(realSum.getSum() / count);
        
        // Threshold the pixels in the neighborhood based on the local average
        for ( final IntType value : localNeighborhood )
        {
        	if (value.getInteger() > avg)
        		value.set(255);
        	
        	else
        		value.set(0);
        	
        }
        
    }
    
    
}
	
	public static void main(String[] args) 
	{
		// open an ImageJ window
		new ImageJ();

		// run the example
		new AdaptiveThreshold();

	}

}
