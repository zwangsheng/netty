/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.bootstrap;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPipelineFactory;
import io.netty.util.DummyHandler;
import org.junit.Test;


/**
 * A bootstrap test
 * 
 * @author <a href="http://netty.io/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 */
public class BootstrapTest {
    
    /**
     * Tests to make sure that a new bootstrap should not return null, but an IllegalStateException
     */
    @Test(expected = IllegalStateException.class)
    public void shouldNotReturnNullFactory() {
        new Bootstrap().getFactory();
    }

    /**
     * Tests to make sure that a new bootstrap cannot have it's channel factory changed
     */
    @Test(expected = IllegalStateException.class)
    public void shouldNotAllowInitialFactoryToChange() {
        new Bootstrap(createMock(ChannelFactory.class)).setFactory(createMock(ChannelFactory.class));
    }

    /**
     * Tests to make sure that a bootstrap's factory can only be set once and not reset after that
     */
    @Test
    public void shouldNotAllowFactoryToChangeMoreThanOnce() {
		//Initialize a new bootstrap
        Bootstrap b = new Bootstrap();
        //Create a mock-up ChannelFactory
        ChannelFactory f = createMock(ChannelFactory.class);
        //Set the bootstrap's channel factory
        b.setFactory(f);
        //Assert that both f and the bootstrap's factory are the same
        assertSame(f, b.getFactory());

        //Start a try block
        try {
			//Attempt to set the bootstrap's channel factory again
            b.setFactory(createMock(ChannelFactory.class));
            //Fail if no exception is thrown
            fail();
        } catch (IllegalStateException e) {
            // Success.
        }
    }

    /**
     * Tests to make sure that setFactory does not accept null as a parameter
     */
    @Test(expected = NullPointerException.class)
    public void shouldNotAllowNullFactory() {
        new Bootstrap().setFactory(null);
    }

    /**
     * Tests to make sure that a new bootstrap's default pipeline is not null
     */
    @Test
    public void shouldHaveNonNullInitialPipeline() {
        assertNotNull(new Bootstrap().getPipeline());
    }

    /**
     * Tests to make sure a bootstrap's pipeline cannot be set to null
     */
    @Test(expected = NullPointerException.class)
    public void shouldNotAllowNullPipeline() {
        new Bootstrap().setPipeline(null);
    }

    /**
     * Tests to make sure a bootstrap cannot have it's pipeline (map) set to null
     */
    @Test(expected = NullPointerException.class)
    public void shouldNotAllowNullPipelineMap() {
        new Bootstrap().setPipelineAsMap(null);
    }

    /**
     * Tests to make sure a bootstrap's initial pipeline factory is not null
     */
    @Test
    public void shouldHaveNonNullInitialPipelineFactory() {
        assertNotNull(new Bootstrap().getPipelineFactory());
    }

    /**
     * Tests to make sure that a bootstrap's pipeline factory changes if a new pipeline is set
     */
    @Test
    public void shouldUpdatePipelineFactoryIfPipelineIsSet() {
		//Make a new bootstrap
        Bootstrap b = new Bootstrap();
        //Get the initial pipeline factory
        ChannelPipelineFactory oldPipelineFactory = b.getPipelineFactory();
        //Set a new pipeline
        b.setPipeline(createMock(ChannelPipeline.class));
        //Assert that the new pipeline factory is not the same as the initial one
        assertNotSame(oldPipelineFactory, b.getPipelineFactory());
    }

    /**
     * Tests to make sure a bootstrap's pipeline is not returned when a pipeline factory is explicitly set
     */
    @Test(expected = IllegalStateException.class)
    public void shouldNotReturnPipelineWhenPipelineFactoryIsSetByUser() {
        Bootstrap b = new Bootstrap();
        b.setPipelineFactory(createMock(ChannelPipelineFactory.class));
        b.getPipeline();
    }

    /**
     * Tests to make sure a bootstrap's pipeline map is not returned when a pipeline factory is explicitly set
     */
    @Test(expected = IllegalStateException.class)
    public void shouldNotReturnPipelineMapWhenPipelineFactoryIsSetByUser() {
        Bootstrap b = new Bootstrap();
        b.setPipelineFactory(createMock(ChannelPipelineFactory.class));
        b.getPipelineAsMap();
    }

    /**
     * Tests to make sure a bootstrap's pipeline factory cannot be set to null
     */
    @Test(expected = NullPointerException.class)
    public void shouldNotAllowNullPipelineFactory() {
        new Bootstrap().setPipelineFactory(null);
    }

    /**
     * Tests to make sure a new bootstrap's pipeline map is empty
     */
    @Test
    public void shouldHaveInitialEmptyPipelineMap() {
        assertTrue(new Bootstrap().getPipelineAsMap().isEmpty());
    }

    /**
     * Tests to make sure that a buffer's pipeline map is ordered
     */
    @Test
    public void shouldReturnOrderedPipelineMap() {
        Bootstrap b = new Bootstrap();
        ChannelPipeline p = b.getPipeline();
        p.addLast("a", new DummyHandler());
        p.addLast("b", new DummyHandler());
        p.addLast("c", new DummyHandler());
        p.addLast("d", new DummyHandler());

        Iterator<Entry<String, ChannelHandler>> m =
            b.getPipelineAsMap().entrySet().iterator();
        Entry<String, ChannelHandler> e;
        e = m.next();
        assertEquals("a", e.getKey());
        assertSame(p.get("a"), e.getValue());
        e = m.next();
        assertEquals("b", e.getKey());
        assertSame(p.get("b"), e.getValue());
        e = m.next();
        assertEquals("c", e.getKey());
        assertSame(p.get("c"), e.getValue());
        e = m.next();
        assertEquals("d", e.getKey());
        assertSame(p.get("d"), e.getValue());

        assertFalse(m.hasNext());
    }

    /**
     * Tests to make sure that a buffer's pipeline map cannot be set to an unordered map
     */
    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowUnorderedPipelineMap() {
        Map<String, ChannelHandler> m = new HashMap<String, ChannelHandler>();
        m.put("a", new DummyHandler());
        m.put("b", new DummyHandler());
        m.put("c", new DummyHandler());
        m.put("d", new DummyHandler());

        Bootstrap b = new Bootstrap();
        b.setPipelineAsMap(m);
    }

    /**
     * Tests to make sure a buffer's pipeline is ordered when it is set from a map
     */
    @Test
    public void shouldHaveOrderedPipelineWhenSetFromMap() {
        Map<String, ChannelHandler> m = new LinkedHashMap<String, ChannelHandler>();
        m.put("a", new DummyHandler());
        m.put("b", new DummyHandler());
        m.put("c", new DummyHandler());
        m.put("d", new DummyHandler());

        Bootstrap b = new Bootstrap();
        b.setPipelineAsMap(m);

        ChannelPipeline p = b.getPipeline();

        assertSame(p.getFirst(), m.get("a"));
        assertEquals("a", p.getContext(p.getFirst()).getName());
        p.removeFirst();
        assertSame(p.getFirst(), m.get("b"));
        assertEquals("b", p.getContext(p.getFirst()).getName());
        p.removeFirst();
        assertSame(p.getFirst(), m.get("c"));
        assertEquals("c", p.getContext(p.getFirst()).getName());
        p.removeFirst();
        assertSame(p.getFirst(), m.get("d"));
        assertEquals("d", p.getContext(p.getFirst()).getName());
        p.removeFirst();

        try {
            p.removeFirst();
            fail();
        } catch (NoSuchElementException e) {
            // Success.
        }
    }

    /**
     * Tests to make sure that a new bootstrap should not have any options set
     */
    @Test
    public void shouldHaveInitialEmptyOptionMap() {
        assertTrue(new Bootstrap().getOptions().isEmpty());
    }

    /**
     * Tests to make sure that a bootstrap's option map updates in real time
     */
    @Test
    public void shouldUpdateOptionMapAsRequested1() {
        Bootstrap b = new Bootstrap();
        b.setOption("s", "x");
        b.setOption("b", true);
        b.setOption("i", 42);

        Map<String, Object> o = b.getOptions();
        assertEquals(3, o.size());
        assertEquals("x", o.get("s"));
        assertEquals(true, o.get("b"));
        assertEquals(42, o.get("i"));
    }

    /**
     * Tests to make sure that a bootstrap's option map updates in real time
     */
    @Test
    public void shouldUpdateOptionMapAsRequested2() {
        Bootstrap b = new Bootstrap();
        Map<String, Object> o1 = new HashMap<String, Object>();
        o1.put("s", "x");
        o1.put("b", true);
        o1.put("i", 42);
        b.setOptions(o1);

        Map<String, Object> o2 = b.getOptions();
        assertEquals(3, o2.size());
        assertEquals("x", o2.get("s"));
        assertEquals(true, o2.get("b"));
        assertEquals(42, o2.get("i"));

        assertNotSame(o1, o2);
        assertEquals(o1, o2);
    }

    /**
     * Tests to make sure that an option is removed from a buffer's options when the option is set to null
     */
    @Test
    public void shouldRemoveOptionIfValueIsNull() {
        Bootstrap b = new Bootstrap();

        b.setOption("s", "x");
        assertEquals("x", b.getOption("s"));

        b.setOption("s", null);
        assertNull(b.getOption("s"));
        assertTrue(b.getOptions().isEmpty());
    }

    /**
     * Tests to make sure that a bootstrap can't accept null as a parameter of getOption(key)
     */
    @Test(expected = NullPointerException.class)
    public void shouldNotAllowNullOptionKeyOnGet() {
        new Bootstrap().getOption(null);
    }

    /**
     * Tests to make sure a bootstrap can't accept a null key when using setOption(key, value)
     */
    @Test(expected = NullPointerException.class)
    public void shouldNotAllowNullOptionKeyOnSet() {
        new Bootstrap().setOption(null, "x");
    }

    /**
     * Tests to make sure that a boostrap can't accept a null option map
     */
    @Test(expected = NullPointerException.class)
    public void shouldNotAllowNullOptionMap() {
        new Bootstrap().setOptions(null);
    }
}
