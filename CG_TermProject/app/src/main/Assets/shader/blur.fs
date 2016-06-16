
precision mediump float;

uniform vec3 u_scale;
uniform sampler2D s_tex0;

varying vec2 v_texCoord;

void main()
{
	vec2 filter0= vec2(-3.0,0.015625);
	vec2 filter1= vec2(-2.0,0.09375);
	vec2 filter2= vec2(-1.0,0.234375);
	vec2 filter3= vec2(0.0,	0.3125);
	vec2 filter4= vec2(1.0,	0.234375);
	vec2 filter5= vec2(2.0,	0.09375);
	vec2 filter6= vec2(3.0,	0.015625);

	vec4 color = vec4(0.0);

	color += texture2D( s_tex0, vec2( v_texCoord.x+filter0.x*u_scale.x, v_texCoord.y+filter0.x*u_scale.y ) )*filter0.y;
	color += texture2D( s_tex0, vec2( v_texCoord.x+filter1.x*u_scale.x, v_texCoord.y+filter1.x*u_scale.y ) )*filter1.y;
    color += texture2D( s_tex0, vec2( v_texCoord.x+filter2.x*u_scale.x, v_texCoord.y+filter2.x*u_scale.y ) )*filter2.y;
    color += texture2D( s_tex0, vec2( v_texCoord.x+filter3.x*u_scale.x, v_texCoord.y+filter3.x*u_scale.y ) )*filter3.y;
    color += texture2D( s_tex0, vec2( v_texCoord.x+filter4.x*u_scale.x, v_texCoord.y+filter4.x*u_scale.y ) )*filter4.y;
    color += texture2D( s_tex0, vec2( v_texCoord.x+filter5.x*u_scale.x, v_texCoord.y+filter5.x*u_scale.y ) )*filter5.y;
    color += texture2D( s_tex0, vec2( v_texCoord.x+filter6.x*u_scale.x, v_texCoord.y+filter6.x*u_scale.y ) )*filter6.y;



	gl_FragColor = color;
}