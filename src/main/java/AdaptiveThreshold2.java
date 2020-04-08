import ij.ImageJ;
 
import io.scif.img.IO;
import io.scif.img.ImgIOException;
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

public class AdaptiveThreshold2 
{

	public < T extends RealType< T > & NativeType< T > > AdaptiveThreshold2() throws ImgIOException
	{
		// open with SCIFIO as a IntType
        Img< IntType > img = IO.openImgs( "bradley_method_01.png", new IntType() ).get( 0 );
        ImageJFunctions.show( img );
		
        //find the adaptive threshold - using the mean of min and max as the threshold
        AdaptiveThreshold( img );
        
        
        ImageJFunctions.show( img );
		
	}
	
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
        
        //final RealSum realSum = new RealSum();
        final IntType min = new IntType( 255 );
        final IntType max = new IntType( 0 );
        double avg = 0 ;
        
        // Find min and max for all the pixels in the neighborhood 
        for ( final IntType value : localNeighborhood )
        {
            //check if min value is smaller than the current pixel value
            if ( value.compareTo( min ) >= 0 )
            {
            	min.set(value);
                
            }
          //check if current pixel value is greater than max 
            if ( value.compareTo( max ) < 0 )
            {
            	max.set(value);
                
            }
        }
        
        avg = ((max.get()+min.get()) / 2)-20;
        
        for ( final IntType value : localNeighborhood )
        {
        	
        	if ((value.getInteger()) <= (avg*0.11))
        	    value.set(0);
        	
        	else
        		value.set(255);
        	
        }
      }
    }

	public static void main(String[] args) 
	{
		// open an ImageJ window
		new ImageJ();

		// run the example
		new AdaptiveThreshold2();

	}
}
