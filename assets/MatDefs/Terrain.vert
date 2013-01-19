uniform mat4 g_WorldViewProjectionMatrix;
attribute vec3 inPosition;
attribute vec2 inTexCoord;
attribute vec4 inColor;
varying vec2 texCoord;
varying float lightLevel;

void main(){
    texCoord = inTexCoord;
    lightLevel = inColor.x;
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
}