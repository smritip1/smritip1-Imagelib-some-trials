/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2016 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
/**
 * License: GPL
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package interactive.remote.openconnectome;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import net.imglib2.Interval;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.volatiles.VolatileRealType;

/**
 * <p>Read pixels served by the
 * <a href="http://hssl.cs.jhu.edu/wiki/doku.php?id=randal:hssl:research:brain:data_set_description">Open
 * Connectome Volume Cutout Service</a>.</p>
 * 
 * <p>The {@link VolatileOpenConnectomeRandomAccessibleInterval} is created with a base
 * URL, e.g.
 * <a href="http://openconnecto.me/emca/kasthuri11">http://openconnecto.me/emca/kasthuri11</a>
 * the interval dimensions, the dimensions of image cubes to be fetched and
 * cached, and an offset in <em>z</em>.  This offset constitutes the
 * 0-coordinate in <em>z</em> and should point to the first slice of the
 * dataset.</p> 
 * 
 * @author Stephan Saalfeld
 */
public class VolatileOpenConnectomeRandomAccessibleInterval extends
		AbstractOpenConnectomeRandomAccessibleInterval< VolatileRealType< UnsignedByteType >, VolatileOpenConnectomeRandomAccessibleInterval.Entry >
{
	public class Entry extends AbstractOpenConnectomeRandomAccessibleInterval< VolatileRealType< UnsignedByteType >, Entry >.Entry
	{
		public boolean valid;
		final public byte[] data;
		
		public Entry( final Key key, final byte[] data, final boolean valid )
		{
			super( key );
			this.data = data;
			this.valid = valid;
		}
		
		public boolean isValid() { return valid; }
		public void setValid( final boolean valid ) { this.valid = valid; }
	}
	
	protected class Fetcher extends Thread
	{
		@Override
		final public void run()
		{
			while ( !isInterrupted() )
			{
				Reference< Entry > ref;
				synchronized ( queue )
				{
					try { ref = queue.pop(); }
					catch ( final NoSuchElementException e ) { ref = null; }
				}
				if ( ref == null )
				{
					synchronized ( this )
					{
						try { wait(); }
						catch ( final InterruptedException e ) {}
					}
				}
				else
				{
					final Entry entry;
					synchronized ( cache )
					{
						entry = ref.get();
						if ( entry != null )
						{
							/* replace WeakReferences by SoftReferences which promotes cache entries from third to second class citizens */
							synchronized ( cache )
							{
								cache.remove( entry.key );
								cache.put( entry.key, new SoftReference< Entry >( entry ) );
							}
						}
					}
					
					if ( entry != null )
					{	
						final long x0 = cellWidth * entry.key.x;
						final long y0 = cellHeight * entry.key.y;
						final long z0 = cellDepth * entry.key.z + minZ;
						
						final StringBuffer url = new StringBuffer( baseUrl );
						url.append( level );
						url.append( "/" );
						url.append( x0 );
						url.append( "," );
						url.append( x0 + cellWidth );
						url.append( "/" );
						url.append( y0 );
						url.append( "," );
						url.append( y0 + cellHeight );
						url.append( "/" );
						url.append( z0 );
						url.append( "," );
						url.append( z0 + cellDepth );
						url.append( "/" );
						
						try
						{
							final URL file = new URL( url.toString() );
							final InputStream in = file.openStream();
							final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
							final byte[] chunk = new byte[ 4096 ];
							int l;
							for ( l = in.read( chunk ); l > 0; l = in.read( chunk ) )
							    byteStream.write( chunk, 0, l );
			
							final byte[] zippedBytes = byteStream.toByteArray();
							final Inflater inflater = new Inflater();
							inflater.setInput( zippedBytes );
							inflater.inflate( entry.data );
							entry.setValid( true );
								
							inflater.end();
							byteStream.close();
							
							//System.out.println( "cached x=" + x + " y=" + y + " z=" + z + " url(" + url.toString() + ")" );
						}
						catch (final IOException e)
						{
							System.out.println( "failed loading x=" + entry.key.x + " y=" + entry.key.y + " z=" + entry.key.z + " url(" + url.toString() + ")" );
						}
						catch( final DataFormatException e )
						{
							System.out.println( "failed unpacking x=" + entry.key.x + " y=" + entry.key.y + " z=" + entry.key.z + " url(" + url.toString() + ")" );
						}
					}
				}
			}
		}
	}
	
	public class VolatileOpenConnectomeRandomAccess extends AbstractOpenConnectomeRandomAccess
	{
		protected Entry entry;
		
		public VolatileOpenConnectomeRandomAccess()
		{
			super( new VolatileRealType< UnsignedByteType >( new UnsignedByteType() ) );
		}
		
		public VolatileOpenConnectomeRandomAccess( final VolatileOpenConnectomeRandomAccess template )
		{
			super( template );
		}
		
		@Override
		public VolatileRealType< UnsignedByteType > get()
		{
			t.get().set( 0xff & entry.data[ ( zMod * cellHeight + yMod ) * cellWidth + xMod ] );
			t.setValid( entry.valid );
			return t;
		}

		@Override
		public VolatileOpenConnectomeRandomAccess copy()
		{
			return new VolatileOpenConnectomeRandomAccess( this );
		}

		@Override
		public VolatileOpenConnectomeRandomAccess copyRandomAccess()
		{
			return copy();
		}
		
		@Override
		protected void fetchPixels()
		{
			entry = VolatileOpenConnectomeRandomAccessibleInterval.this.fetchPixels( xDiv, yDiv, zDiv );
		}
	}
	
	final protected Fetcher fetcher;
	final protected LinkedList< Reference< Entry > > queue = new LinkedList< Reference< Entry > >();
	
	public VolatileOpenConnectomeRandomAccessibleInterval( final String url, final long width, final long height, final long depth, final int cellWidth, final int cellHeight, final int cellDepth, final long minZ, final int level )
	{
		super( url, width, height, depth, cellWidth, cellHeight, cellDepth, minZ, level );
		
		fetcher = new Fetcher();
		fetcher.start();
	}
	
	public VolatileOpenConnectomeRandomAccessibleInterval( final String url, final long width, final long height, final long depth, final long minZ, final int level )
	{
		this( url, width, height, depth, 64, 64, 64, minZ, level );
	}
	
	public VolatileOpenConnectomeRandomAccessibleInterval( final String url, final long width, final long height, final long depth, final int level )
	{
		this( url, width, height, depth, 0, level );
	}
	
	@Override
	public int numDimensions()
	{
		return 3;
	}
	
	
	@Override
	public VolatileOpenConnectomeRandomAccess randomAccess()
	{
		return new VolatileOpenConnectomeRandomAccess();
	}
	
	@Override
	public VolatileOpenConnectomeRandomAccess randomAccess( final Interval interval )
	{
		return randomAccess();
	}
		
	@Override
	protected Entry fetchPixels2( final long x, final long y, final long z )
	{
		final Reference< Entry > ref;
		final Key key;
		synchronized ( cache )
		{
			key = new Key( x, y, z );
			final Reference< Entry > cachedReference = cache.get( key );
			if ( cachedReference != null )
			{
				final Entry cachedEntry = cachedReference.get();
				if ( cachedEntry != null )
					return cachedEntry;
			}
			
			final byte[] bytes = new byte[ cellWidth * cellHeight * cellDepth ];
			ref = new WeakReference< Entry >( new Entry( key, bytes, false ) );
			//ref = new SoftReference< Entry >( new Entry( key, bytes, false ) );
			cache.put( key, ref );
			queue.add( ref );
		}
		synchronized ( fetcher )
		{
			fetcher.notify();
		}
		
		final Entry entry = ref.get();
		if ( entry != null )
			return entry;
		else
			return new Entry( key, new byte[ cellWidth * cellHeight * cellDepth ], false );
	}
	
	@Override
	public void finalize()
	{
		fetcher.interrupt();
	}
}
