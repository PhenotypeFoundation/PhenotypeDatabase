package dbnp.data

class FeatureBase {

	String name
	String unit

	static hasMany = [
		metabolite: Term,
		enzyme: Term,
		organismPart: Term,
		compound: Term,
		drug: Term,
		disease: Term]

	static constraints = {
	}
}
