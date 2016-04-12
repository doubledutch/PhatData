def pdreduce(obj,agg):
	if obj['test']<10:
		return agg

	count=0
	if 'count' in agg:
		count=agg['count']

	count=count+1

	agg['count']=count
	return agg