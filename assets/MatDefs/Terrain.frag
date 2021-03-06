uniform sampler2D m_Terrain;
uniform float m_DayAlpha;
uniform float m_PulseLightAlpha;
uniform vec4 m_SunColor;
uniform vec4 m_MoonColor;
varying vec2 texCoord;

varying float sunLightLevel;
varying float constantRedLightLevel;
varying float constantGreenLightLevel;
varying float constantBlueLightLevel;
varying float pulseRedLightLevel;
varying float pulseGreenLightLevel;
varying float pulseBlueLightLevel;

void main(){
    vec4 color = texture2D( m_Terrain, texCoord );
    float xMax = color.x * color.x;
    float yMax = color.y * color.y;
    float zMax = color.z * color.z;
    vec4 black = vec4(0.0,0.0,0.0,1.0);

    vec4 sunLight = mix(black, vec4(color.x*m_SunColor.x, color.y*m_SunColor.y, color.z*m_SunColor.z, 1.0), sunLightLevel*m_DayAlpha);
    vec4 moonLight = mix(black, vec4(color.x*m_MoonColor.x, color.y*m_MoonColor.y, color.z*m_MoonColor.z, 1.0), sunLightLevel*(1-m_DayAlpha));
    vec4 naturalLight = vec4((sunLight.x+moonLight.x)/(1+(sunLight.x*moonLight.x/xMax)), (sunLight.y+moonLight.y)/(1+(sunLight.y*moonLight.y/yMax)), (sunLight.z+moonLight.z)/(1+(sunLight.z*moonLight.z/zMax)), 1.0);
    
    vec4 constantColoredLight = vec4(color.x*constantRedLightLevel, color.y*constantGreenLightLevel, color.z*constantBlueLightLevel, 1.0f);
    vec4 pulseColoredLight = mix(black, vec4(color.x*pulseRedLightLevel, color.y*pulseGreenLightLevel, color.z*pulseBlueLightLevel, 1.0f), m_PulseLightAlpha);   
    vec4 coloredLight = vec4((constantColoredLight.x+pulseColoredLight.x)/(1+constantColoredLight.x*pulseColoredLight.x/xMax), (constantColoredLight.y+pulseColoredLight.y)/(1+constantColoredLight.y*pulseColoredLight.y/yMax), (constantColoredLight.z+pulseColoredLight.z)/(1+constantColoredLight.z*pulseColoredLight.z/zMax), 1.0 ); 
    
    vec4 combinedLight = vec4((naturalLight.x+coloredLight.x)/(1+naturalLight.x*coloredLight.x/xMax), (naturalLight.y+coloredLight.y)/(1+naturalLight.y*coloredLight.y/yMax), (naturalLight.z+coloredLight.z)/(1+naturalLight.z*coloredLight.z/zMax), 1.0 );
    gl_FragColor = combinedLight;
}
