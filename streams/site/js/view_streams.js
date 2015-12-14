function renderStream(stream){
	if(stream.get('topic').indexOf('_phatdata.')==0){
		return '';
	}
	var buf=''
	buf+='<div class="stream">'
	buf+='<h1>'+stream.get('topic')+'</h1>'
	buf+='<p>count: '+stream.get('count').toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",")+' size: '
	var size=stream.get('size')
	if(size<4096){
		buf+=size+' bytes'
	}else if(size<1024*1024){
		buf+=Math.floor(size/102.4)/10+' kb'
	}else if(size<1024*1024*1024){
		buf+=Math.floor(size/(1024*102.4))/10+' mb'
	}else{
		buf+=Math.floor(size/(1024*1024*102.4))/10+' gb'
	}
	buf+='</p>'
	buf+='</div>'
	return buf
}

function renderStreamsPage(data){
	var buf=''
	var streams=data.get('streams')
	for(var i=0;i<streams.size;i++){
		var stream=streams.get(i)
		buf+=renderStream(stream)
	}
	$('#content').html(buf)
}