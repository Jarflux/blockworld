uniform mat4 g_WorldViewProjectionMatrix;
attribute vec3 inPosition;
attribute vec2 inTexCoord;
attribute vec4 inColor;
varying vec2 texCoord;
varying float sunLightLevel;
varying float constantRedLightLevel;
varying float constantGreenLightLevel;
varying float constantBlueLightLevel;
varying float pulseRedLightLevel;
varying float pulseGreenLightLevel;
varying float pulseBlueLightLevel;

vec3 unpackColor(float f){
    float r = floor(f); 
    f = (f - r) * 256.0;       
    float g = floor(f);
    f = (f - g) * 256.0; 
    float b = floor(f);   
    return vec3(r/256.0,g/256.0,b/256.0); 
}

void main(){
    texCoord = inTexCoord;
    vec3 rgbColorsConstantLight = unpackColor(inColor.x);   
    constantRedLightLevel = rgbColorsConstantLight.x;    
    constantGreenLightLevel = rgbColorsConstantLight.y;
    constantBlueLightLevel = rgbColorsConstantLight.z;
    
    vec3 rgbColorsPulseLight = unpackColor(inColor.y);
    pulseRedLightLevel = rgbColorsPulseLight.x;    
    pulseGreenLightLevel = rgbColorsPulseLight.y;
    pulseBlueLightLevel = rgbColorsPulseLight.z;
    
    sunLightLevel = inColor.a;
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
}

