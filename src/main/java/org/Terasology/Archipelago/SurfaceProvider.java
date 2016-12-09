/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.Terasology.Archipelago;

import org.terasology.entitySystem.Component;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.utilities.procedural.*;
import org.terasology.world.generation.*;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

@Produces(SurfaceHeightFacet.class)
public class SurfaceProvider implements ConfigurableFacetProvider{

    private Noise surfaceNoise, surfaceNoise2, surfaceNoise3;

    private ArchipelagoConfiguration configuration = new ArchipelagoConfiguration();

    @Override
    public void setSeed(long seed) {
        surfaceNoise = new SubSampledNoise(new SimplexNoise(seed - 23214), new Vector2f(0.01f, 0.01f), 1);
        surfaceNoise2 = new SubSampledNoise(new BrownianNoise(new SimplexNoise(seed + 3), 8), new Vector2f(0.022f, 0.022f), 1);
        surfaceNoise3 = new SubSampledNoise(new SimplexNoise(seed - 13), new Vector2f(0.001f, 0.001f), 1);
    }

    @Override
    public void process(GeneratingRegion region) {

        Border3D border = region.getBorderForFacet(SurfaceHeightFacet.class);
        SurfaceHeightFacet facet = new SurfaceHeightFacet(region.getRegion(), border);
        Rect2i processRegion = facet.getWorldRegion();


        for (BaseVector2i position : processRegion.contents()) {
            float surreal =  1/(1+(float) Math.exp(-configuration.surrealism*position.length())*(10-1));
            float height = TeraMath.clamp(surfaceNoise.noise(position.x(), position.y())*10 + surfaceNoise2.noise(position.x(), position.y()*10), -5, 10);
            float val = TeraMath.clamp(surfaceNoise.noise(position.x(), position.y()) / 3 + surfaceNoise2.noise(position.x(), position.y()) / 3 + surfaceNoise3.noise(position.x(), position.y() / 3), 0, 1);
            if (((val > 0.25-surreal/10 && val < 0.65+surreal/10))) {
                facet.setWorld(position, height);
            } else {
                facet.setWorld(position, -100);
            }
        }

        region.setRegionFacet(SurfaceHeightFacet.class, facet);
    }

    @Override
    public String getConfigurationName() {
        return "Archipelago";
    }

    @Override
    public Component getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(Component configuration) {
        this.configuration = (ArchipelagoConfiguration)configuration;
    }

    private static class ArchipelagoConfiguration implements Component {
        @Range(min = 0.005f, max = 1f, increment = 0.005f, precision = 3, description = "Surrealism")
        private float surrealism = 0.05f;
    }
}
